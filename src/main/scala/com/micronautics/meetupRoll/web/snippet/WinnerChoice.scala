package com.micronautics.meetupRoll.web.snippet

import net.liftweb._
import http._
import common._
import util.Helpers._
import js._
import JsCmds._
import JE._
import net.liftweb.util
import java.util
import net.liftweb.util
import scala.collection.immutable.ListMap
import net.liftweb.http.SHtml._
import xml.{NodeSeq, Elem}
import net.liftweb.util._
import com.typesafe.config.{ConfigObject, ConfigFactory}
import com.micronautics.meetupRoll.PrizeRules
import com.micronautics.meetupRoll.web.snippet.ParticipantCrowd.actualNumberOfParticipants

/**
 * @author Julia Astakhova
 */
object WinnerChoice {

  object winners extends SessionVar[List[Winner]](List[Winner]())
  object remainingPrizes extends SessionVar[Map[Prize, Int]](new ListMap())
}

class WinnerChoice {

  import WinnerChoice._

  private val sponsors = ConfigFactory.load("sponsors")
  private val prizesData = sponsors.getList("prizeRules")
  private val prizeRules = prizesData.toArray.toList.collect {case c:ConfigObject => c} .map (PrizeRules.apply)

  remainingPrizes.set(new ListMap() ++ prizeRules.map(rule => Prize(
    rule.name,
    rule.forNumberOfParticipants(actualNumberOfParticipants.getOrElse
    {throw new IllegalStateException("No actual number of participants specified")})))
    .filter(_.quantity > 0)
    .map(prize => (prize -> prize.quantity)))

  var currentWinner: Winner = pickWinner()

  def currentWinnerNode = <span>{currentWinner.name}</span>
  def currentPrizeNode = <span>{currentWinner.prize.name}</span>

  def currentPrizesNode = <div class="span4 offset2 well prizes">
    <div class="row"><strong class="span4 text-center prizelabel">Prizes</strong></div><ol>{
      remainingPrizes.get.keys.map(prize =>
        <li><em>{prize.name}</em></li>)
      }</ol></div>

  def winnersNode = <table class="table table-striped">
    {winners.get.sortBy(_.prize).map(winner =>
      <tr><td><strong>{winner.name}</strong></td><td>won</td><td><strong>{winner.prize}</strong></td></tr>)}
  </table>

  def pickWinner(): Winner = Winner(Meetup.pickWinner(), remainingPrizes.head._1.name)

  def render = {
    def updateWinner(): JsCmd = {
      if (remainingPrizes.get.isEmpty)
        JsHideId("winnerChoice") & JsHideId("currentPrizes") & SetHtml("winners", winnersNode)
      else {
        currentWinner = pickWinner()
        SetHtml("currentWinner", currentWinnerNode) & SetHtml("currentPrize", currentPrizeNode) &
          SetHtml("winners", winnersNode) & SetHtml("currentPrizes", currentPrizesNode)
      }
    }

    val choice: CssSel = if (!remainingPrizes.get.isEmpty) {
      "@currentPrizes" #> currentPrizesNode &
      "@currentWinner" #> currentWinnerNode &
      "@currentPrize" #> currentPrizeNode &
      "@choiceYes" #> ajaxButton("Yes, the person is here", () => {
        winners.set(currentWinner :: winners.get)
        val (currentPrize, remainingQuantity) = remainingPrizes.head
        remainingPrizes.set(remainingPrizes.get - currentPrize)
        if (remainingQuantity > 1)
          remainingPrizes.set(new ListMap() + (currentPrize -> (remainingQuantity - 1)) ++ remainingPrizes.get)
        updateWinner()
      }) &
      "@choiceNo" #> ajaxButton("No", () => updateWinner)
    } else
      ClearClearable

    "@winners" #> winnersNode & choice
  }
}

case class Winner(name: String, prize: String)

case class Prize(name: String, quantity: Int)
