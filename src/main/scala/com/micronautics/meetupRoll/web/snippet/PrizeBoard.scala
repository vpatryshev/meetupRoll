package com.micronautics.meetupRoll.web.snippet

import net.liftweb.http._
import js.JE.ValById
import js.JsCmds.SetHtml
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds
import xml.NodeSeq

/**
 * Created with IntelliJ IDEA.
 * User: Julia
 * Date: 9/4/13
 */
class PrizeBoard {

  import WinnerChoice.prizeList

  def prizeBoardNode: NodeSeq = {
    def quantityInputNode(prize: Prize) =
      ajaxText(prize.quantity.toString, (quantity) => {
        prizeList.set(Prize(prize.name, quantity.toInt) :: prizeList.get.filterNot(_ == prize))
        JsCmds.Noop
      })

    def removePrizeButtonNode(prize: Prize) =
      ajaxButton(<i class="icon-remove"></i>, () => {
        prizeList.set(prizeList.get.filterNot(_ == prize))
        SetHtml("prizeBoard", prizeBoardNode)
      }, "class" -> "btn ovalbtn")

    def addPrizeButtonNode() =
      ajaxButton(<i class="icon-ok"></i>, ValById("addPrize"), (prizeName: String) => {
        prizeList.set(Prize(prizeName, 1) :: prizeList.get)
        SetHtml("prizeBoard", prizeBoardNode)
      }, "class" -> "btn ovalbtn")

    <table class="table">
      {prizeList.get.sortBy(_.name).map(prize =>
        <tr>
          <td class="notop"><strong>{prize.name}</strong></td>
          <td class="notop">{quantityInputNode(prize)}</td>
          <td class="notop">{
            if (prizeList.get.length > 1) removePrizeButtonNode(prize)
            else ""
            }</td>
        </tr>)
      }
      <tr>
        <td class="notop"><input id="addPrize" type="text" placeholder="Add new prize name..."/></td>
        <td class="notop">{addPrizeButtonNode()}</td><td class="notop"></td>
      </tr>
    </table>
  }

  def render = {
    if (prizeList.get.isEmpty)
      S.redirectTo("/")

    "@prizeBoard" #> prizeBoardNode &
    "@prizeBoardFinished" #> ajaxButton("Finish", () => {
      WinnerChoice.reload()
      S.redirectTo("winners.html")
    }, "class" -> "btn btn-success")
  }
}
