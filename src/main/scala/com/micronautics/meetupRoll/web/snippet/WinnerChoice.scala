package com.micronautics.meetupRoll.web.snippet

import net.liftweb._
import http._
import util.Helpers._
import js._
import JsCmds._
import JE._
import scala.collection.immutable.ListMap
import http.SHtml._
import xml.{NodeSeq, Elem}
import net.liftweb.util._
import com.typesafe.config.{ConfigObject, ConfigFactory}
import com.micronautics.meetupRoll.PrizeRules
import com.micronautics.meetupRoll.web.snippet.ParticipantCrowd.actualNumberOfParticipants
import com.micronautics.util.Mailer
import scala.Some
import scala.util.{Try, Success, Failure}

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

  var currentWinner: Attendant = Meetup.pickWinner()

  def currentWinnerNode = <span>{currentWinner.name}</span>

  def sendNode: NodeSeq = {
    def sendButtonNode =
      ajaxButton("Send", () => {
        def handleError(e: Throwable): JsCmd = {
          e.printStackTrace()
          val alert = NodeUtil.alertError("Error trying to send the letter [" + e.getMessage + "]")
          SetHtml("send", <span>{alert}</span><span>{sendNode}</span>)
        }

        EmailSettingsPage.emailSettings.get.flatMap(settings => Try {
          val winString = winners.get map (w => (w.person.name + ": " + w.prize)) mkString("Winners are:\n  ", "\n  ", "\n")
          new Mailer().sendMail(
            settings.email, settings.smtpHost, settings.smtpSender, settings.smtpPwd, "Giveaway winners", winString)
          SetHtml("send", NodeUtil.alertSuccess("The letter was successfully sent."))}
        ) match {
          case Failure(e) => handleError(e)
          case Success(cmd) => cmd
        }
      }, "class" -> "btn ovalbtn btn-success")

    <span>{sendButtonNode}</span><span class="help-inline">
      Email is {EmailSettingsPage.emailSettings.get.map(_.email).getOrElse("undefined")}</span>
  }

  private def updateWinner(): JsCmd = {
    if (remainingPrizes.get.get.isEmpty) {
      JsHideId("winnerChoice") & SetHtml("winners", winnersNode) & SetHtml("send", sendNode)
    } else {
      currentWinner = Meetup.pickWinner()
      SetHtml("currentWinner", currentWinnerNode) & SetHtml("winners", winnersNode) &
        SetHtml("currentPrizes", currentPrizesAndPhotoNode)
    }
  }

  def currentPrizesAndPhotoNode = {
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
    <span class="row">
      {
        currentWinner.photo.map(photo =>
                  <span class="span2 prizephoto"><img src={photo}/></span>
            ).getOrElse(
                  <span class="span2"/>)
      }
      <div class="span4 offset2 well prizes">
        <h4 class="prizelabel text-center">Prizes</h4>

        <ol>{remainingPrizeList.sortBy(_.name).map(prize => <li>{prizeButtonNode(prize)}</li>)}</ol>
      </div>
    </span>
  }

  def winnersNode =
    <table class="table table-striped">
      {winners.get.sortBy(_.prize).map(winner =>
        <tr>
          {winner.person.thumbnail.map(photo => <td><img src={photo} width="40"/></td>).getOrElse(<td/>)}
          <td><strong>{winner.person.name}</strong></td>
          <td>won</td>
          <td><strong>{winner.prize}</strong></td>
        </tr>)
      }
  </table>


  def render = {
    val choice: CssSel = if (!remainingPrizes.get.get.isEmpty) {
      "@text1" #> "is a winner. Choose a prize:" &
      "@text2" #> "Or mark if the person is not here:" &
      "@currentPrizes" #> currentPrizesAndPhotoNode &
      "@currentWinner" #> currentWinnerNode &
      "@choiceNo" #> ajaxButton("Not here", () => updateWinner, "class" -> "ovalbtn btn btn-danger")
    } else if (!winners.get.isEmpty)
      "@send" #> sendNode
    else
      ClearClearable

    "@winners" #> winnersNode & choice
  }
}

case class Winner(person: Attendant, prize: String)

case class Prize(name: String, quantity: Int)
