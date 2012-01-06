package com.micronautics.meetupRoll

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}


object MeetupRoll extends App {
  val groupUrl = "http://www.meetup.com/Bay-Area-Scala-Enthusiasts/events/43944352/printrsvp/?togglePhotos=off&pop=true"
  val httpclient = new DefaultHttpClient
  val Names = """<span class="D_name">(\S+) (\S*)\n""".r
  
  val names = (Names findAllIn httpGet(groupUrl)).matchData foreach {
    m => println(m.group(1) + " " + m.group(2))
  }

  /** Fetches contents of web page pointed to by urlStr */
  def httpGet(urlStr:String):String = {
    val httpget = new HttpGet(urlStr)
    val brh = new BasicResponseHandler
    httpclient.execute(httpget, brh)
  }
}