package com.micronautics.meetupRoll.web.snippet

import xml._
import com.typesafe.config.{ConfigObject, ConfigFactory}
import java.net.{URL, URLDecoder}
import scalax.io.Codec
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

  var meetupAPI: MeetupAPI = MeetupAPIStub

  object chosenMeetup extends SessionVar[Try[MeetupData]](
    meetupAPI.chooseDefaultEvent().flatMap(meetupAPI.loadMeetup(_)))
  object participantCrowd extends SessionVar[mutable.Buffer[Attendant]](MeetupData.unbox(meetup).participants.toBuffer)

  val random = new Random()

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
        chosenMeetup.set(meetupAPI.loadMeetup(found.next().group(1)))
        reloadParticipantCrowd()
        S.redirectTo("/")
      } else
        SetHtml("error", NodeUtil.alertError("URL doesn't lead to a meetup page."))
    }, "class" -> "btn btn-success")
  }
}

case class Attendant(name: String, photo: Option[String], thumbnail: Option[String])

object MeetupData {

  val stub = new MeetupData("Meetup information is unreachable", List.empty)

  def unbox(tried: Try[MeetupData]): MeetupData = tried match {
    case Success(x) => x
    case Failure(error) => {
      error.printStackTrace()
      stub
    }
  }
}

case class MeetupData(id: String, title: String, participants: List[Attendant]) {
  def this(title: String, participants: List[Attendant]) = this("", title, participants)
}



