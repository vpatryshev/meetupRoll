package com.micronautics.meetupRoll

import java.net.URL
import java.util.Properties

import scala.collection.mutable.ListMap
import scala.util.Random

import com.micronautics.util.SendMailUsingAuthentication

import scalax.io.JavaConverters.asInputConverter
import scalax.io.Codec


object MeetupRoll extends App {
  private val random = new Random()
  private val Names = """<span class="D_name">(\S+) (\S*)""".r
  private val properties = readProps
  private val meetupGroup = Option(properties.getProperty("meetupGroup")).orElse(Some("Bay-Area-Scala-Enthusiasts")).get
  private val eventId     = Option(properties.getProperty("eventId"))    .orElse(Some("44582312")).get
  
  private def groupUrl = "http://www.meetup.com/" + meetupGroup + "/events/" + eventId + "/printrsvp"
  private val attendeesPage = new URL(groupUrl).asInput.slurpString(Codec.UTF8)

  /** Mutable list of full names. If a member did not specify a last name they will not appear in the list.
    * Names that are chosen are removed so they cannot be chosen again. */
  private var names = (for (m <- (Names findAllIn attendeesPage).matchData)
    yield m.group(1) + " " + m.group(2)).toList.toBuffer

  private def numNames = names.length
  
  private def randomName = names(random.nextInt(numNames))
  
  private def readProps = {
    val properties = new Properties()
    val in = MeetupRoll.getClass().getClassLoader().getResourceAsStream("meetup.properties")
    if (in!=null) {
      properties.load(in)
      in.close()
    }
    properties
  }
    
  val aPage = attendeesPage
  var winners = new ListMap[String,String].empty
  println("Parsed " + names.length + " names from " + groupUrl)
  while (true) {
    val name = randomName
    names -= name
    println("Winner: " + name)
    val token = Console.readLine("Type the name of the prize " + name + " won, or type Enter to exit> ")
    if (token=="") {
      var winString = "Winners are:\n";
      for (winner <- winners) 
        winString += "  " + winner._1 + ": " + winner._2 + "\n"
      println(winString + "\nSending email so you remember...")
      val mailer = new SendMailUsingAuthentication();
      mailer.postMail(Array("mslinn@micronauticsresearch.com"), "Giveaway winners", winString, "mslinn@micronauticsresearch.com")
      println("Done.")
      System.exit(0)
    }
    winners += name -> token
  }
}