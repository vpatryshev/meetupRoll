package com.micronautics.meetupRoll

import java.net.URL

import scala.collection.mutable.ListMap
import scala.util.Random

import com.micronautics.util.SendAuthenticatedEMail

import scalax.io.JavaConverters.asInputConverter
import scalax.io.Codec
import java.util.{Date, Calendar, Properties}
import java.text.{ParseException, SimpleDateFormat}


object MeetupRoll extends App {
  private val random = new Random()
  private val Date = """<span class="date">(.*)</span>""".r
  private val Title = """<title>Meetup.com &rsaquo; RSVP List: (.*)</title>""".r
  private val Names = """<span class="D_name">(\S+) (\S*)""".r
  private val properties = readProps
  private val meetupGroup = Option(properties.getProperty("meetupGroup")).orElse(Some("Bay-Area-Scala-Enthusiasts")).get
  private val eventId = Option(properties.getProperty("eventId")).orElse(Some("44582312")).get

  private def groupUrl = "http://www.meetup.com/" + meetupGroup + "/events/" + eventId + "/printrsvp"

  private val attendeesPage = new URL(groupUrl).asInput.slurpString(Codec.UTF8)
  private val title = (Title findFirstMatchIn attendeesPage) match {
    case Some(x) => x group (1)
    case None => ""
  }

  private val date = (Date findFirstMatchIn attendeesPage) match {
    case Some(x) => x group (1)
    case None => ""
  }

  /**Mutable list of full names. If a member did not specify a last name they will not appear in the list.
   * Names that are chosen are removed so they cannot be chosen again. */
  private var names = (for (m <- (Names findAllIn attendeesPage).matchData)
  yield m.group(1) + " " + m.group(2)).toList.toBuffer

  private def numNames = names.length

  private def randomName = names(random.nextInt(numNames))

  private def readProps = {
    val properties = new Properties()
    val in = MeetupRoll.getClass().getClassLoader().getResourceAsStream("meetup.properties")
    if (in == null) {
      System.err.println("Could not read meetup.properties, aborting.");
      System.exit(1);
    } else {
      properties.load(in)
      in.close()
    }
    properties
  }

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
  println(groupUrl)
  if (!isEventToday(date))
    println("\n*** THIS EVENT IS NOT HELD TODAY ***\n")
  while (true) {
    val name = randomName
    names -= name
    println("Winner: " + name)
    val token = Console.readLine("Type the name of the prize " + name + " won, or type Enter to exit > ")
    if (token == "") {
      if (winners.size > 0) {
        var winString = "Winners are:\n";
        for (winner <- winners)
          winString += "  " + winner._1 + ": " + winner._2 + "\n"
        println(winString + "\nSending email so you remember...")
        SendAuthenticatedEMail.sendEmail(properties.getProperty("smtpUser"), "Giveaway winners", winString, properties.getProperty("smtpUser"))
      }
      println("Done.")
      System.exit(0)
    }
    winners += name -> token
  }
}