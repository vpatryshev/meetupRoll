package com.micronautics.meetupRoll.web.snippet

import com.micronautics.meetupRoll.web.snippet.WinnerChoice.prizeList
import net.liftweb.http.{SHtml, SessionVar, S}
import net.liftweb.http.SHtml._
import com.typesafe.config.{Config, ConfigFactory}
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
 * Created with IntelliJ IDEA.
 * User: Julia
 * Date: 9/6/13
 */
object EmailSettingsPage {
  object emailSettings extends SessionVar[EmailSettings](new EmailSettings(ConfigFactory.load("meetup")))
}

class EmailSettingsPage {
  import EmailSettingsPage.emailSettings

  def render = {
    var email = emailSettings.get.email
    var host = emailSettings.get.smtpHost
    var pwd = emailSettings.get.smtpPwd

    def process() {
      emailSettings.set(EmailSettings(emailSettings.get.toSend, email, host, pwd))
      S.redirectTo("winners.html")
    }

    "@email"    #> text(email, email = _) &
    "@smtpHost" #> text(host, host = _) &
    "@smtpPwd"  #> password(pwd, pwd = _) &
    "type=submit" #> SHtml.onSubmitUnit(process)
  }
}

case class EmailSettings(toSend: Boolean, email: String, smtpHost: String, smtpPwd: String) {
  def this(config: Config) =
    this(false, config.getString("smtpUser"), config.getString("smtpHost"), config.getString("smtpPwd"))
}