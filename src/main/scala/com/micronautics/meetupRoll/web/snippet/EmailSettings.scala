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
import scala.util.Try


/**
 * Created with IntelliJ IDEA.
 * User: Julia
 * Date: 9/6/13
 */
object EmailSettingsPage {
  object emailSettings extends SessionVar[Try[EmailSettings]](Try(new EmailSettings(ConfigFactory.load())))
}

class EmailSettingsPage {
  import EmailSettingsPage.emailSettings

  def render = {
    var email = emailSettings.get.map(_.email).getOrElse("undefined")

    def process() {
      emailSettings.set(emailSettings.get.map(_.copy(email = email)))
      S.redirectTo("winners.html")
    }

    "@email"    #> text(email, email = _) &
    "type=submit" #> SHtml.onSubmitUnit(process)
  }
}

case class EmailSettings(toSend: Boolean, email: String, smtpHost: String, smtpSender: String, smtpPwd: String) {
  def this(config: Config) =
    this(false,
      config.getString("smtpUser"),
      config.getString("smtpHost"),
      config.getString("smtpSender"),
      config.getString("smtpPwd"))
}