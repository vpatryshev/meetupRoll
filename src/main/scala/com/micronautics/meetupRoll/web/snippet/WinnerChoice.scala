package com.micronautics.meetupRoll.web.snippet

import net.liftweb._
import http._
import common._
import js.JsCmds.SetHtml
import js.JsCmds.SetHtml
import util.Helpers._
import js._
import JsCmds._
import JE._
import net.liftweb.util
import java.util
import net.liftweb.util
import scala.collection.immutable.ListMap
import http.SHtml._
import xml.{NodeSeq, Elem}
import net.liftweb.util._
import com.typesafe.config.{ConfigObject, ConfigFactory}
import com.micronautics.meetupRoll.PrizeRules
import com.micronautics.meetupRoll.web.snippet.ParticipantCrowd.actualNumberOfParticipants
import com.micronautics.meetupRoll.web.snippet.Prize
import com.micronautics.meetupRoll.web.snippet.Winner
import com.micronautics.util.Mailer
import com.micronautics.meetupRoll.web.snippet.Prize
import scala.Some
import com.micronautics.meetupRoll.web.snippet.Winner

/**
 * @author Julia Astakhova
 */
object WinnerChoice {

  object winners extends SessionVar[List[Winner]](List[Winner]())
  object prizeList extends SessionVar[List[Prize]](List[Prize]())
  object remainingPrizes extends SessionVar[Option[Map[Prize, Int]]](None)

  def reload() {
    def loadPrizesFromConfig() {
      val sponsors = ConfigFactory.load("sponsors")
      val prizesData = sponsors.getList("prizeRules")
      val prizeRules = prizesData.toArray.toList.collect {case c:ConfigObject => c} .map (PrizeRules.apply)

      prizeList.set(prizeRules.map(rule => Prize(
        rule.name,
        rule.forNumberOfParticipants(actualNumberOfParticipants.getOrElse
        {throw new IllegalStateException("No actual number of participants specified")})))
        .filter(_.quantity > 0))
    }

    if (prizeList.get.isEmpty)
      loadPrizesFromConfig()

    Meetup.reloadParticipantCrowd()
    remainingPrizes.set(Some(Map() ++ prizeList.get.map(prize => (prize -> prize.quantity))))
    winners.set(List[Winner]())
  }
}

class WinnerChoice {

  import WinnerChoice._

  var currentWinner: String = Meetup.pickWinner()

  def currentWinnerNode = <span>{currentWinner.name}</span>

  def sendNode: NodeSeq = {
    def sendButtonNode =
      ajaxButton("Send", () =>
        try {
          val winString = winners.get map (w => (w.name + ": " + w.prize)) mkString("Winners are:\n  ", "\n  ", "\n")
          val settings = EmailSettingsPage.emailSettings.get
          new Mailer().sendMail(settings.email, settings.smtpHost, settings.smtpPwd, "Giveaway winners", winString)
          SetHtml("send", NodeUtil.alertSuccess("The letter was successfully sent."))
        } catch {
          case e  =>
            e.printStackTrace()
            val alert = NodeUtil.alertError("Error trying to send the letter [" + e.getMessage + "]")
            SetHtml("send", <span>{alert}</span><span>{sendNode}</span>)
        }
      , "class" -> "btn ovalbtn btn-success")

    <span>{sendButtonNode}</span><span class="help-inline">Email is {EmailSettingsPage.emailSettings.get.email}</span>
  }

  private def updateWinner(): JsCmd = {
    if (remainingPrizes.get.get.isEmpty) {
      JsHideId("winnerChoice") & SetHtml("winners", winnersNode) & SetHtml("send", sendNode)
    } else {
      currentWinner = Meetup.pickWinner()
      SetHtml("currentWinner", currentWinnerNode) & SetHtml("winners", winnersNode) &
        SetHtml("currentPrizes", currentPrizesNode)
    }
  }

  def currentPrizesNode = {
    val remainingPrizeList = List() ++ remainingPrizes.get.get.keys

    def prizeButtonNode(prize: Prize) =
      ajaxButton(prize.name, () => {
        winners.set(Winner(currentWinner, prize.name) :: winners.get)
        val remainingQuantity: Int = remainingPrizes.get.get(prize)
        remainingPrizes.set(Some(remainingPrizes.get.get - prize))
        if (remainingQuantity > 1)
          remainingPrizes.set(Some(new ListMap() + (prize -> (remainingQuantity - 1)) ++ remainingPrizes.get.get))
        updateWinner()
      }, "class" -> "btn btn-success ovalbtn prizelabel")

    <div class="span4 offset2 well prizes">
      <div class="row"><strong class="span4 text-center prizelabel">Prizes</strong></div>
      <ol>{remainingPrizeList.sortBy(_.name).map(prize => <li>{prizeButtonNode(prize)}</li>)}</ol>
    </div>
  }

  def winnersNode =
    <table class="table table-striped">
      {winners.get.sortBy(_.prize).map(winner =>
        <tr>
          <td><strong>{winner.name}</strong></td>
          <td>won</td>
          <td><strong>{winner.prize}</strong></td>
        </tr>)
      }
  </table>


  def render = {
    val choice: CssSel = if (!remainingPrizes.get.get.isEmpty) {
      "@text1" #> "is a winner. Choose a prize:" &
      "@text2" #> "Or mark if the person is not here:" &
      "@currentPrizes" #> currentPrizesNode &
      "@currentWinner" #> currentWinnerNode &
      "@choiceNo" #> ajaxButton("Not here", () => updateWinner, "class" -> "ovalbtn btn btn-danger")
    } else if (!winners.get.isEmpty)
      "@send" #> sendNode
    else
      ClearClearable

    "@winners" #> winnersNode & choice
  }
}

case class Winner(name: String, prize: String)

case class Prize(name: String, quantity: Int)
