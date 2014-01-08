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
import java.text.{Normalizer, ParseException, SimpleDateFormat}
import java.io.{PrintWriter, FileWriter, File, FileOutputStream}
import java.util
import javax.management.remote.rmi._RMIConnection_Stub
import xml.NodeSeq


object MeetupRoll extends App {
  private val random = new Random()
  // Gui.startup(null)

  /** Events on front page
    * TODO parse them and find today's event, or the next upcoming one, or the most recent one if none are upcoming */
  private val Event = """<a href="http://www.meetup.com/.*?/events/(.*?)/" itemprop="url" class="omnCamp omnrv_rv13"><span itemprop="summary">(.*?)</span></a>""".r
  private val Date = """<span class="date">(.*)</span>""".r
  private val Title = """<title>Meetup.com &rsaquo; RSVP List: (.*)</title>""".r
  private val Names = """<span class="D_name">([^<]*)""".r
  val config = ConfigFactory.load("meetup")
  private val eventId = "154014822"//config.getString("eventId")
  private val meetupGroup = "Scala-Bay"//config.getString("meetupGroup")
  val sponsors = ""// ConfigFactory.load("sponsors")
  private val prizesData = List[String]()//sponsors.getList("prizeRules")
  private val prizeRules = prizesData.toArray.toList//.collect {case c:ConfigObject => c} .map (PrizeRules.apply)
  println(prizeRules)
  private val mailer = new Mailer()
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

  private val title = (Title findFirstMatchIn attendeesPage) map (_ group (1)) getOrElse "?"

  private val date = (Date findFirstMatchIn attendeesPage) map (_ group (1)) getOrElse "?"

  def intact = Set("", "I", "II", "III", "IV", "V")

  def isConsonant(c: Char) = "bcdfghjklmnpqrstvwxz" contains c.toLower

  def nameCase(rawName: String): String = {
    val name = rawName.trim
    if (intact(name)) name else
    if (name contains "-") (name split "-" map nameCase mkString "-")
    else {
      val (h,t) = name.toList.splitAt(1)
      val l0 = h.head
      t match {
        case l1::Nil if (isConsonant(l0) && isConsonant(l1)) => "" + l0.toUpper + l1.toUpper
        case '\''::tail => h.head.toUpper + '\'' + nameCase(tail.mkString)
        case _          => h.head.toUpper + t.mkString.toLowerCase
      }
    }
  }

  def fixName(name: String) = {
    if (name.contains("Grimaldi")) {
      println("wtf")
    }
    val names = name split " " toList
    val tail = names.tail
    val first = nameCase(names.head)
    if (tail.isEmpty) first else {
      first::(tail.dropRight(1)):::List(nameCase(tail.last)) mkString " "
    }
  }


  /**Mutable list of full names. If a member did not specify a last meetupName they will not appear in the list.
    * Names that are chosen are removed so they cannot be chosen again. */
  private var names = (for (m <- (Names findAllIn attendeesPage).matchData)
  yield m.group(1)).toSet.toBuffer map fixName sorted

  def findName(s: String) = names.find(_.contains(s))

  private def numNames = names.length
  val nPerPage = 60

  def firstName(name: String) = name split " " head
  def escape(xmlText: String): NodeSeq = {
    def escapeChar(c: Char): xml.Node =
      if (c > 0x7F || Character.isISOControl(c))
        xml.EntityRef("#" + Integer.toString(c, 10))
      else
        xml.Text(c.toString)

    new xml.Group(xmlText.map(escapeChar(_)))
  }
  def inCell(text:String) = <p style="margin-left:4px;margin-right:4px;">{escape(text)}</p>
  def th(text: String) = <th>{inCell(text)}</th>
  def tr(name:String) = <tr><td>{inCell(name)}</td><td></td></tr>
  val col1 = th("Name        (ORDER BY FirstName)")
  val col2 = th("Your Signature or Something")
  def table(list:List[String]) = if (list.isEmpty)(<p></p>) else <center><table border="1"><tr>{col1}{col2}</tr>{list map tr}</table></center>

  def printRoster =
  /*  if (Console.readLine("Want to prepare the roster for printing? >").toLowerCase.startsWith("y"))*/ {
    val out = new PrintWriter(new FileWriter(new File("meetup." + new SimpleDateFormat("yyyy-MM-dd").format(new Date) + ".html")))
    out.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"></head><style>\n.break { page-break-before: always; }\n</style><body>")
    names.toList.grouped(nPerPage).zipWithIndex.foreach{case (list:List[String], pageNo) =>
      out.println(s"<h3>${date}. <i>${title}</i> </h3>")
      val name1 = firstName(list.head)
      val name2 = firstName(list.last)
      out.println(<center>-- {pageNo+1}  --</center>)
      out.println(<center><b>{name1}..{name2}</b></center>)
      val (col1,col2) = list.splitAt(nPerPage/2)
      out.println(<table width ="100%"><tr><td valign="top">{table(col1)}</td><td valign="top">{table(col2)}</td></tr></table>)
      out.println(<p class="break"/>)
    }
    out.println("</body></html>")
    out.close
  }

  printRoster

  def dumpRoster =
  /*  if (Console.readLine("Want to prepare the roster for printing? >").toLowerCase.startsWith("y"))*/ {
    val out = new PrintWriter(new FileWriter(new File("meetup." + new SimpleDateFormat("yyyy-MM-dd").format(new Date) + ".csv")))
    out.write(names.toList.mkString(","))
    out.close
  }
  dumpRoster

  def normalizeName(name: String) = {
    Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "")  }

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
      val visibleName = normalizeName(name)
      val anybodyHere = Console.readLine("Here's the winner: " + visibleName + ", is this person around? > ")
      if (anybodyHere.equalsIgnoreCase("y")) return Some(visibleName)
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
    val numPresent = Console.readLine("Signed up " + numNames + "... how many are here? >").toInt/*
    for (rule <- prizeRules) {
      val numPrizes = rule.forNumberOfParticipants(numPresent)
      println("--RULE " + rule + "--> " + numPrizes)
      pickWinners(numPrizes, rule)
    }
    if (winners.size > 0) {
      val winString = winners map (w => (w._1 + ": " + w._2)) mkString("Winners are:\n  ", "\n  ", "\n")
      println(winString + "\nSending email so you remember...")
      mailer.sendMail(
        config.getString("smtpUser"),
        config.getString("smtpHost"),
        config.getString("smtpUser"),
        config.getString("smtpPwd"),
        "Giveaway winners",
        winString)
    }*/
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
          mailer.sendMail(
            config.getString("smtpUser"),
            config.getString("smtpHost"),
            config.getString("smtpUser"),
            config.getString("smtpPwd"),
            "Giveaway winners",
            winString)
        }
        println("Done.")
        System.exit(0)
      }
      winners += name -> token
    }
  }
}