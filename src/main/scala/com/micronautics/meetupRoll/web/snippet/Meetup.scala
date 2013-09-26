package com.micronautics.meetupRoll.web.snippet

import xml._
import com.typesafe.config.{ConfigObject, ConfigFactory}
import java.net.{URL, URLDecoder}
import scalax.io.Codec
import scalax.io.JavaConverters.asInputConverter
import net.liftweb.util.BindHelpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds.SetHtml
import util._
import java.text.Normalizer
import net.liftweb.http.{S, SessionVar}
import collection.mutable
import net.liftweb.http.js.{JsCmds, JsCommands, JsCmd}
import net.liftweb.http.js.JE.ValById
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.json._
import net.liftweb.http.js.JE.ValById
import util.Success
import net.liftweb.http.js.JsCmds.SetHtml
import com.micronautics.meetupRoll.web.snippet.Attendant

/**
 * @author Julia Astakhova
 */
object Meetup {

  object chosenMeetup extends SessionVar[Try[MeetupData]](chooseDefaultEvent().flatMap(loadMeetup(_)))
  object participantCrowd extends SessionVar[mutable.Buffer[Attendant]](
    MeetupData.unbox(chosenMeetup.get).participants.toBuffer)

  val random = new Random()

  val config = Try(ConfigFactory.load("meetup"))
  val meetupGroup = config.map(_.getString("meetupGroup")).getOrElse("")
  val apiKey = Try(ConfigFactory.load()).map(_.getString("apiKey")).getOrElse("")
  val apiUrl = "http://api.meetup.com/2"

  implicit val formats = DefaultFormats

  def loadPage(url: String) = new URL(url).asInput.string(Codec.UTF8)

  def chooseDefaultEvent(): Try[String] = {
    def calendarPage = loadPage(s"$apiUrl/events?group_urlname=$meetupGroup&sign=true&key=$apiKey" +
      "&status=past,upcoming&time=-3m,1m")

    case class Event(time: String, id: String)
    case class EventList(results: List[Event])

    config.flatMap(_ => Try {
      val events = parse(calendarPage).extract[EventList].results
      events.minBy(event => Math.abs(System.currentTimeMillis() - event.time.toLong)).id
    })
  }

  def loadMeetup(eventId: String): Try[MeetupData] = {
    def attendeesPage(eventId: String) = loadPage(s"$apiUrl/rsvps?key=$apiKey&event_id=$eventId&rsvp=yes")

    case class Member(name: String)
    case class MemberPhoto(photo_link: String, thumb_link: String)
    case class Event(name: String)
    case class Entry(member: Member, member_photo: Option[MemberPhoto], event: Event)
    case class EntryList(results: List[Entry])

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

    Try {
      val members = parse(attendeesPage(eventId)).extract[EntryList].results
      val title = members.head.event.name
      val attendants = members.map(entry =>
        Attendant(fixName(
          entry.member.name), entry.member_photo.map(_.photo_link), entry.member_photo.map(_.thumb_link))).
        sortBy(_.name)

      MeetupData(title, attendants)
    }
  }

  private def meetup = chosenMeetup.get

  private def participantNumber = MeetupData.unbox(chosenMeetup.get).participants.length

  private def attendants = participantCrowd.get

  def pickWinner(): Attendant = {
    def normalizeName(name: String) = {
      Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "")  }

    val randomAttendant = attendants(random.nextInt(attendants.length))

    participantCrowd.set(attendants - randomAttendant)
    randomAttendant.copy(name = normalizeName(randomAttendant.name))
  }

  def reloadParticipantCrowd() = participantCrowd.set(MeetupData.unbox(meetup).participants.toBuffer)
}

class Meetup {

  import Meetup._

  // methods used on index.html

  val title = MeetupData.unbox(meetup).title
  val titleNodes: NodeSeq = <span>{title}</span>

  val participantNumberNodes: NodeSeq = <span>{participantNumber}</span>
}

class MeetupChoosing {

  import Meetup._

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

case class Attendant(name: String, photo: Option[String], thumbnail: Option[String])

object MeetupData {

  val stub = MeetupData("Meetup information is unreachable", List.empty)

  def unbox(tried: Try[MeetupData]): MeetupData = tried match {
    case Success(x) => x
    case Failure(error) => {
      error.printStackTrace()
      stub
    }
  }
}

case class MeetupData(title: String, participants: List[Attendant])



