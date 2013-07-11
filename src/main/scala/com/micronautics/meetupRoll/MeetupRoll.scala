package com.micronautics.meetupRoll

/* Copyright 1012 Micronautics Research Corporation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Subject to the additional condition that the attribution code in Gui.scala
   remains untouched and displays each time the program runs.

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

import java.net.URL

import scala.collection.mutable.ListMap
import scala.util.Random
import com.typesafe.config._
import com.micronautics.util.Mailer

import scalax.io.JavaConverters.asInputConverter
import scalax.io.Codec
import java.util.{Date, Calendar, Properties}
import java.text.{ParseException, SimpleDateFormat}
import java.io.{PrintWriter, FileWriter, File, FileOutputStream}
import java.util


object MeetupRoll extends App {
  private val random = new Random()
 // Gui.startup(null)

  /** Events on front page
   * TODO parse them and find today's event, or the next upcoming one, or the most recent one if none are upcoming */
  private val Event = """<a href="http://www.meetup.com/.*?/events/(.*?)/" itemprop="url" class="omnCamp omnrv_rv13"><span itemprop="summary">(.*?)</span></a>""".r
  private val Date = """<span class="date">(.*)</span>""".r
  private val Title = """<title>Meetup.com &rsaquo; RSVP List: (.*)</title>""".r
  private val Names = """<span class="D_name">(\S+) (\S*)""".r
  val config = ConfigFactory.load("meetup")
  private val eventId = config.getString("eventId")
  private val meetupGroup = config.getString("meetupGroup")
  val sponsors = ConfigFactory.load("sponsors")
  val xxx = sponsors.getString("xxx")
  private val prizesData = sponsors.getList("prizeRules")
  private val prizeRules = prizesData.toArray.toList.collect {case c:ConfigObject => c} .map (PrizeRules.apply)
  println(prizeRules)
  private val mailer = new Mailer(config)
  private def groupUrl = "http://www.meetup.com/" + meetupGroup
  private def eventUrl = groupUrl + "/events/" + eventId + "/printrsvp"

//  private val calendarPage = new URL(groupUrl + "/events/calendar").asInput.string

  private val EventUrlPattern = ("a href=\"" + groupUrl + "/events/([\\d]+)/\"").r
/*
  calendarPage match {
    case EventUrlPattern(id) => println(s"Hurray! found event id $id"); System.exit(1)
    case x => println(s"Alas, $x"); System.exit(1)
  }
*/
  private val attendeesPage = new URL(eventUrl).asInput.string(Codec.UTF8)

  private val title = (Title findFirstMatchIn attendeesPage) match {
    case Some(x) => x group (1)
    case None => ""
  }

  private val date = (Date findFirstMatchIn attendeesPage) match {
    case Some(x) => x group (1)
    case None => ""
  }

  /**Mutable list of full names. If a member did not specify a last meetupName they will not appear in the list.
   * Names that are chosen are removed so they cannot be chosen again. */
  private var names = (for (m <- (Names findAllIn attendeesPage).matchData)
  yield m.group(1) + " " + m.group(2)).toList.toBuffer

  private def numNames = names.length
  val nPerPage = 70

  def inCell(text:String) = <p style="margin-left:4px;margin-right:4px;">{text}</p>
  def th(text: String) = <th>{inCell(text)}</th>
  def tr(name:String) = <tr><td>{inCell(name)}</td><td></td></tr>
  val col1 = th("Name (ORDER BY FirstName)")
  val col22 = th("Your Signature or Something")
  def table(list:List[String]) = if (list.isEmpty)(<p></p>) else <center><table border="1"><tr>{col1}{col22}</tr>{list map tr}</table></center>

  if (Console.readLine("Want to prepare the roster for printing? >").toLowerCase.startsWith("y")) {
    val out = new PrintWriter(new FileWriter(new File("meetup." + new SimpleDateFormat("yyyy-MM-dd").format(new Date) + ".html")))
    out.println("<html><body>")
    names.toList.grouped(nPerPage).zipWithIndex.foreach{case (list:List[String], pageNo) =>
      out.println(<center>--{pageNo+1}--</center>)
      val (col1,col2) = list.splitAt(nPerPage/2)
      out.println(<table width ="100%"><tr><td valign="top">{table(col1)}</td><td valign="top">{table(col2)}</td></tr></table>)
      out.println(<p class="break"/>)
    }
    out.println("</body></html>")
    out.close
  }


  private def randomName = names(random.nextInt(numNames))

  private def isEventToday(date: String): Boolean = {
    val eventDate: Date = try {
      new SimpleDateFormat("EEEE, MMMMM dd, yyyy HH:mm a").parse(date)
    } catch {
      case pe: ParseException =>
        println(pe)
        System.exit(1)
        new Date()
    }
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.setTime(new Date())
    cal2.setTime(eventDate)
    val sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
      cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    sameDay
  }

  val aPage = attendeesPage
  var winners = new ListMap[String, String].empty
  println("\n\nParsed " + names.length + " names from \"" + title + "\"")
  println("Scheduled for " + date)
  println(eventUrl)
  if (!isEventToday(date))
    println("\n*** THIS EVENT IS NOT HELD TODAY ***\n")

  runByRules

  def pickWinner: Option[String] = {
    while (!names.isEmpty) {
      val name = randomName
      names -= name
      val anybodyHere = Console.readLine("Here's the winner: " + name + ", is this person around? > ")
      if (anybodyHere.equalsIgnoreCase("y")) return Some(name)
      if (anybodyHere.isEmpty) return None
    }
    return None
  }

  def pickWinners(numPrizes: Int, rule: PrizeRules) {
    for (i <- 0 to numPrizes) {
      val nameOpt = pickWinner
      nameOpt match {
        case Some(name) => {
          println("Winner: " + name)
          val sponsor = rule.sponsor
          val prize = rule.name
          winners += name -> (prize + " from " + sponsor)
        }
        case None => return
      }
    }
  }

  def runByRules {
    println("--RUNNING BY RULES--")
    val numPresent = Console.readLine("Signed up " + numNames + "... how many are here? >").toInt
    for (rule <- prizeRules) {
      val numPrizes = rule.forNumberOfParticipants(numPresent)
      println("--RULE " + rule + "--> " + numPrizes)
      pickWinners(numPrizes, rule)
    }
    if (winners.size > 0) {
      val winString = winners map (w => (w._1 + ": " + w._2)) mkString("Winners are:\n  ", "\n  ", "\n")
      println(winString + "\nSending email so you remember...")
      mailer.sendMail(config.getString("smtpUser"), config.getString("smtpUser"), "Giveaway winners", winString)
    }
    println("Done.")
    System.exit(0)

  }

  //  runInteractively

  def runInteractively {
    while (true) {
      val name = randomName
      names -= name
      println("Winner: " + name)
      val token = Console.readLine("Type the name of the prize " + name + " won, or type Enter to exit > ")
      if (token == "") {
        if (winners.size > 0) {
          val winString = winners map (w => (w._1 + ": " + w._2)) mkString("Winners are:\n  ", "  ", "\n")
          println(winString + "\nSending email so you remember...")
          mailer.sendMail(config.getString("smtpUser"), config.getString("smtpUser"), "Giveaway winners", winString)
        }
        println("Done.")
        System.exit(0)
      }
      winners += name -> token
    }
  }
}