package com.micronautics.meetupRoll.web.snippet

import xml.{Utility, NodeSeq}
import com.typesafe.config.{ConfigObject, ConfigFactory}
import java.net.{URLDecoder, URL}
import scalax.io.Codec
import scalax.io.JavaConverters.asInputConverter
import net.liftweb.util.BindHelpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.SetHtml
import util.Random
import java.text.Normalizer
import net.liftweb.http.{S, SessionVar}
import net.liftweb.common.Box
import collection.mutable
import com.micronautics.meetupRoll.web.snippet.WinnerChoice.prizeList
import scala.Some
import com.micronautics.meetupRoll.web.snippet.MeetupData
import net.liftweb.http.js.{JsCmds, JsCommands, JsCmd}
import net.liftweb.http.js.JE.ValById

/**
 * @author Julia Astakhova
 */
object Meetup {
  object choosenMeetup extends SessionVar[Option[MeetupData]](None)

  val random = new Random()

  def loadMeetup(eventId: String = ConfigFactory.load("meetup").getString("eventId")): MeetupData = {
    val Event = """<a href="http://www.meetup.com/.*?/events/(.*?)/" itemprop="url" class="omnCamp omnrv_rv13"><span itemprop="summary">(.*?)</span></a>""".r
    val Date = """<span class="date">(.*)</span>""".r
    val Title = """<h1>(.*)</h1>""".r
    val Names = """<span class="D_name">([^<]*)""".r
    val config = ConfigFactory.load("meetup")
    val meetupGroup = config.getString("meetupGroup")
    def groupUrl = "http://www.meetup.com/" + meetupGroup
    def eventUrl(eventId: String) = groupUrl + "/events/" + eventId + "/printrsvp"
    def attendeesPage(eventId: String) = new URL(eventUrl(eventId)).asInput.string(Codec.UTF8)

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

    val title = (Title findFirstMatchIn attendeesPage(eventId)) map (_ group (1)) getOrElse "?"
    val names = (
      for (m <- (Names findAllIn attendeesPage(eventId)).matchData) yield m.group(1)
      ).toSet.toBuffer map fixName sorted

    MeetupData(title, names)
  }

  def meetup() = {
    if (choosenMeetup.isEmpty)
      choosenMeetup.set(Some(loadMeetup()))
    choosenMeetup.is.get
  }

  def names() = meetup.names

  def participantNumber() = names.length

  def pickWinner(): String = {
    def normalizeName(name: String) = {
      Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "")  }

    def randomName = names()(random.nextInt(participantNumber))

    val name = randomName
    choosenMeetup.set(Some(MeetupData(meetup().title, names() - name)))
    normalizeName(name)
  }
}

class Meetup {

  import Meetup._

  val title = meetup().title
  val titleNodes: NodeSeq = <span>{title}</span>

  val participantNumberNodes: NodeSeq = <span>{participantNumber()}</span>

  def render = {
    def alertError = <div class="alert alert-error"><strong>Warning!</strong> URL doesn't lead to a meetup page.</div>
    def alertSuccess = <div class="alert alert-success">Processing...</div>

    "@meetupOK" #> ajaxButton(<span>Ok</span>, ValById("meetup"), (url: String) => {
        val found = """.*/events/(\d+)/""".r.findAllIn(url).matchData
        if (found.hasNext) {
          val meetup = loadMeetup(found.next().group(1))
          choosenMeetup.set(Some(meetup))
          S.redirectTo("index.html")
        } else {
          SetHtml("error", alertError)
        }
    }, "class" -> "btn btn-success")
  }
}

case class MeetupData(title: String, names: mutable.Buffer[String])



