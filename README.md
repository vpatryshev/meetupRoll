Pulls down names of attendees for a specific meetup, then selects a random attendee name for giveaways.
Run this as a console app this way:

    sbt 'run-main com.micronautics.meetupRoll.MeetupRoll'

Run this as a web server with

    ./sbt
    > container:start

... and restart with

    > ~;container:start; container:reload /
