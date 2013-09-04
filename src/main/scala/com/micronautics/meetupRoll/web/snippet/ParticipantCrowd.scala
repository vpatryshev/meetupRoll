package com.micronautics.meetupRoll.web.snippet

import net.liftweb.http.SHtml._

import net.liftweb._
import http._
import common._
import util.Helpers._
import js._
import JsCmds._
import JE._
import scala.xml.NodeSeq
import net.liftweb.util

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
      WinnerChoice.loadPrizes()
      S.redirectTo("winners.html")
    }

    "@numOfActualPart" #> (text("", input => { actualNumberOfParticipants.set(Some(input.toString.toInt)) })
        ++ hidden(process))
  }
}


