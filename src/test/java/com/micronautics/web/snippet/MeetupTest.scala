package com.micronautics.web.snippet

import org.scalatest.{Matchers, FlatSpec}
import util.Success

/**
 * @author Julia Astakhova
 */
class MeetupTest extends FlatSpec with Matchers {

  import com.micronautics.meetupRoll.web.snippet.Meetup._

  "Meetup class" should "load meetup" in {
    val loaded = loadMeetup("140071182")
    loaded.isSuccess should be (true)

    val meetupData = loaded.get
    meetupData.participants.filter(_.photo.isDefined).length should be > (0)
  }
}
