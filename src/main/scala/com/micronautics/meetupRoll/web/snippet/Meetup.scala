package com.micronautics.meetupRoll.web.snippet

import xml._
import com.typesafe.config.{ConfigObject, ConfigFactory}
import java.net.{URL, URLDecoder}
import scalax.io.Codec
import scalax.io.JavaConverters.asInputConverter
import net.liftweb.util.BindHelpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.SetHtml
import scala.util.{Sorting, Random}
import java.text.Normalizer
import net.liftweb.http.{S, SessionVar}
import net.liftweb.common.Box
import collection.mutable
import com.micronautics.meetupRoll.web.snippet.WinnerChoice.prizeList
import scala.Some
import com.micronautics.meetupRoll.web.snippet.MeetupData
import net.liftweb.http.js.{JsCmds, JsCommands, JsCmd}
import net.liftweb.http.js.JE.ValById
import java.util
import collection.mutable.ArrayBuffer
import xml.parsing.NoBindingFactoryAdapter
import org.xml.sax.InputSource
import net.liftweb.http.js.JE.ValById
import com.micronautics.meetupRoll.web.snippet.MeetupData
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JE.ValById
import com.micronautics.meetupRoll.web.snippet.MeetupData
import net.liftweb.http.js.JsCmds.SetHtml
import java.io.StringReader
import scala.util.parsing.json.JSON

/**
 * @author Julia Astakhova
 */
object Meetup {

  object chosenMeetup extends SessionVar[MeetupData](loadMeetup())
  object participantCrowd extends SessionVar[mutable.Buffer[String]](chosenMeetup.get.names.toBuffer)

  val random = new Random()

  val config = ConfigFactory.load("meetup")
  val meetupGroup = config.getString("meetupGroup")

  def loadPage(url: String) = new URL(url).asInput.string(Codec.UTF8)

  def chooseDefaultEvent(): String = {
    val apiKey = "585e76272e2c69481bc44447c2d5c6a"
    val calendarPage = loadPage("http://api.meetup.com/2/events?group_urlname=" + meetupGroup + "&sign=true&key=" + apiKey +
      "&status=past,upcoming&time=-3m,1m")

    case class Event(time: String, id: String)

    case class EventList(results: List[Event])

    import net.liftweb.json._

    implicit val formats = DefaultFormats
    val events = parse(calendarPage).extract[EventList].results
    events.minBy(event => Math.abs(System.currentTimeMillis() - event.time.toLong)).id
  }

  def loadMeetup(eventId: String = chooseDefaultEvent()): MeetupData = {
    def attendeesPage(eventId: String) = loadPage("http://www.meetup.com/" + meetupGroup + "/events/" + eventId + "/printrsvp")

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
      val names = name.split(" ").toList
      val tail = names.tail
      val first = nameCase(names.head)
      if (tail.isEmpty) first else {
        first::(tail.dropRight(1)):::List(nameCase(tail.last)) mkString " "
      }
    }

    val title = ("""<h1>(.*)</h1>""".r findFirstMatchIn attendeesPage(eventId)) map (_ group (1)) getOrElse "?"
    val names =
      (for (m <- ("""<span class="D_name">([^<]*)""".r findAllIn attendeesPage(eventId)).matchData) yield m.group(1))
        .toList.map(fixName).sorted

    MeetupData(title, names, names.length)
  }

  private def meetup = chosenMeetup.get

  private def participantNumber = chosenMeetup.get.names.length

  private def names = participantCrowd.get

  def pickWinner(): String = {
    def normalizeName(name: String) = {
      Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "")  }

    val name = names(random.nextInt(names.length))

    participantCrowd.set(names - name)
    normalizeName(name)
  }

  def reloadParticipantCrowd() = participantCrowd.set(meetup.names.toBuffer)
}

class Meetup {

  import Meetup._

  // methods used on index.html

  val title = meetup.title
  val titleNodes: NodeSeq = <span>{title}</span>

  val participantNumberNodes: NodeSeq = <span>{participantNumber}</span>

  //method used on "choose another meetup" page

  def render = {
    "@meetupOK" #> ajaxButton(<span>Ok</span>, ValById("meetup"), (url: String) => {
        val found = """.*/events/(\d+)/""".r.findAllIn(url).matchData
        if (found.hasNext) {
          chosenMeetup.set(loadMeetup(found.next().group(1)))
          reloadParticipantCrowd()
          S.redirectTo("/")
        } else
          SetHtml("error", NodeUtil.alertError("URL doesn't lead to a meetup page."))
    }, "class" -> "btn btn-success")
  }
}

case class MeetupData(title: String, names: List[String], participantCount: Int)



