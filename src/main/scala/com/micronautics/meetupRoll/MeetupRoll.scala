package com.micronautics.meetupRoll

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import scala.tools.jline.console.ConsoleReader
import scala.util.Random


object MeetupRoll extends App {
  val brh = new BasicResponseHandler
  val groupUrl = "http://www.meetup.com/Bay-Area-Scala-Enthusiasts/events/43944352/printrsvp/?togglePhotos=off&pop=true"
  val httpclient = new DefaultHttpClient
  val random = new Random()
  val Names = """<span class="D_name">(\S+) (\S*)\n""".r
  
  val names = (for (m <- (Names findAllIn httpGet(groupUrl)).matchData)
    yield m.group(1) + " " + m.group(2)).toIndexedSeq
  
  def roll():String = names(random.nextInt(names.length))
  

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
  def console(handler: JLineEvent => Boolean) {
    val consoleReader = new ConsoleReader()
    var finished = false
    while (!finished) {
      val line = consoleReader.readLine(roll())
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
  def httpGet(urlStr:String):String =
    httpclient.execute(new HttpGet(urlStr), brh)
}