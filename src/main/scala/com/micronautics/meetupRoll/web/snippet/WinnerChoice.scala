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

/**
 * @author Julia Astakhova
 */
object WinnerChoice {

  object winners extends SessionVar[List[Winner]](List[Winner]())
  object remainingPrizes extends SessionVar[Map[Prize, Int]](new ListMap())
}

class WinnerChoice {

  import WinnerChoice._

  remainingPrizes.set(new ListMap() ++ PrizeCollection.prizes.getOrElse(Nil).map(prize => (prize -> prize.quantity)))

  var currentWinner: Winner = pickWinner()

  def currentWinnerNode = <span>{currentWinner.name}</span>
  def currentPrizeNode = <span>{currentWinner.prize.name}</span>

  def winnersNode = <table class="table table-striped">
    {winners.get.sortBy(_.prize).map(winner =>
      <tr><td><strong>{winner.name}</strong></td><td>won</td><td><strong>{winner.prize}</strong></td></tr>)}
  </table>

  def pickWinner(): Winner = Winner(Meetup.pickWinner(), remainingPrizes.head._1.name)

  def render = {
    def updateWinner(): JsCmd = {
      if (remainingPrizes.get.isEmpty)
        JsHideId("winnerChoice") & SetHtml("winners", winnersNode)
      else {
        currentWinner = pickWinner()
        SetHtml("currentWinner", currentWinnerNode) & SetHtml("currentPrize", currentPrizeNode) &
          SetHtml("winners", winnersNode)
      }
    }

    val choice: CssSel = if (!remainingPrizes.get.isEmpty) {
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
