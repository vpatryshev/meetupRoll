package com.micronautics.meetupRoll.web.snippet

import net.liftweb.http.SHtml._

import net.liftweb._
import http._
import common._
import util.Helpers._
import js._

/**
 * @author Julia Astakhova
 */
object ParticipantCrowd {
  object actualNumberOfParticipants extends SessionVar[Option[Int]](None)
}

class ParticipantCrowd {
  import ParticipantCrowd._

  def render = {
    def process() = {
      WinnerChoice.reload()
      S.redirectTo("winners.html")
    }

    "@numOfActualPart" #> (
      text("", input => { actualNumberOfParticipants.set(Some(input.toString.toInt)) }, "class" -> "input-mini")
        ++ hidden(process))
  }
}


