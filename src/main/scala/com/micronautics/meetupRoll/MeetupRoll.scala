package com.micronautics.meetupRoll

import java.util.Properties
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import scala.tools.jline.console.ConsoleReader
import scala.util.Random


object MeetupRoll extends App {
  private val brh = new BasicResponseHandler
  private val httpclient = new DefaultHttpClient
  private val random = new Random()
  private val Names = """<span class="D_name">(\S+) (\S*)\n""".r
  private val properties = readProps
  private val meetupGroup = Option(properties.getProperty("meetupGroup")).orElse(Some("Bay-Area-Scala-Enthusiasts")).get
  private val eventId     = Option(properties.getProperty("eventId"))    .orElse(Some("44582312")).get
  
  private def groupUrl = "http://www.meetup.com/" + meetupGroup + "/events/" + eventId + "/printrsvp"

  private def names = (for (m <- (Names findAllIn httpGet(groupUrl)).matchData)
    yield m.group(1) + " " + m.group(2)).toIndexedSeq

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
  
  println("Parsing names from " + groupUrl)
  

  sealed trait JLineEvent
  case class Line(value: String) extends JLineEvent
  case object EmptyLine extends JLineEvent
  case object EOF extends JLineEvent

  console {
    case EOF => 
      true
    case Line(s) if s == "q" => 
      true
    case _ => 
      false
  }

  /** bug: hits EOF right away, so only prints one name then stops */
  private def console(handler: JLineEvent => Boolean) {
    val consoleReader = new ConsoleReader()
    var finished = false
    while (!finished) {
      val line = consoleReader.readLine(randomName)
      if (line == null) {
        finished = handler(EOF)
      } else if (line.size == 0) {
        finished = handler( EmptyLine )
      } else if (line.size > 0) {
        finished = handler( Line( line ) )
      }
    }
  }
  
  /** Fetches contents of web page pointed to by urlStr */
  private def httpGet(urlStr:String):String =
    httpclient.execute(new HttpGet(urlStr), brh)
}