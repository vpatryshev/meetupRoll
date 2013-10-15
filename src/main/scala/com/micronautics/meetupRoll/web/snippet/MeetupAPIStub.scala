package com.micronautics.meetupRoll.web.snippet

import util.Try
import net.liftweb.json._
import com.micronautics.meetupRoll.web.snippet.Attendant
import java.net.URL
import scalax.io.Codec
import scalax.io.JavaConverters.asInputConverter
import com.typesafe.config.ConfigFactory
import java.util.{Random, Collections}
import java.util

/**
 * @author Julia Astakhova
 */
trait MeetupAPI {

  def chooseDefaultEvent(): Try[String]

  def loadMeetup(eventId: String): Try[MeetupData]

}

object RealMeetupAPI extends MeetupAPI {

  def loadPage(url: String) = new URL(url).asInput.string(Codec.UTF8)

  implicit val formats = DefaultFormats

  val config = Try(ConfigFactory.load("meetup"))
  val meetupGroup = config.map(_.getString("meetupGroup")).getOrElse("")
  val apiKey = Try(ConfigFactory.load()).map(_.getString("apiKey")).getOrElse("")
  val apiUrl = "http://api.meetup.com/2"

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
}

object MeetupAPIStub extends MeetupAPI {
  def chooseDefaultEvent(): Try[String] = Try("")

  def loadMeetup(eventId: String): Try[MeetupData] = Try {
    val names = List("Brad", "Cole", "Wendie", "Denny", "Boris", "John", "Sally", "Bob", "Alice", "Ken", "Barbie")
    val surnames = List("Kensington", "Hewlett", "Packard", "Bell", "Clinton", "Livermore", "Wong", "Sadness")

    val rand = new Random()
    def next[T](list: List[T]): T = list(Math.abs(rand.nextInt()) % list.length)

    val attendants = (1 to 50).foldLeft(List[String]())((res, _) => (next(names) + " " + next(surnames)) :: res).
      map(Attendant(_, None, None))

    MeetupData("The most glorious meetup", attendants)
  }
}
