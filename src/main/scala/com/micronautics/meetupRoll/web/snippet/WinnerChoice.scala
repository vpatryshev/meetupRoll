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

  def winnersNode = <table>
    {winners.get.sortBy(_.prize).map(winner => <tr><td>{winner.name}</td><td>won</td><td>{winner.prize}</td></tr>)}
  </table>

  def pickWinner(): Winner = Winner(Meetup.pickWinner(), remainingPrizes.head._1.name)

  def render = {
    val choices = List("yes", "no")

    val choice: CssSel = if (!remainingPrizes.get.isEmpty) {
      "@currentWinner" #> currentWinnerNode &
      "@currentPrize" #> currentPrizeNode &
      "@choice" #> ajaxRadio[String](choices, None, (resp) => {
        if (resp == choices.head) {
          winners.set(currentWinner :: winners.get)
          val (currentPrize, remainingQuantity) = remainingPrizes.head
          remainingPrizes.set(remainingPrizes.get - currentPrize)
          if (remainingQuantity > 1)
            remainingPrizes.set(new ListMap() + (currentPrize -> (remainingQuantity-1)) ++ remainingPrizes.get)
        }
        if (remainingPrizes.get.isEmpty)
          JsHideId("winnerChoice") & SetHtml("winners", winnersNode)
        else {
          currentWinner = pickWinner()
          SetHtml("currentWinner", currentWinnerNode) & SetHtml("currentPrize", currentPrizeNode) &
            SetElemById("radio", JE.boolToJsExp(false), "checked") & SetHtml("winners", winnersNode)
        }
      }, "id" -> "radio").toForm
    } else
      ClearClearable

    "@winners" #> winnersNode & choice
  }
}

case class Winner(name: String, prize: String)
