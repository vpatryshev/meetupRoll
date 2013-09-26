Pulls down names of attendees for a specific meetup, then selects a random attendee name for giveaways.
Run this as a console app this way:

    sbt 'run-main com.micronautics.meetupRoll.MeetupRoll'

Run this as a web server with

    ./sbt
    > container:start

... and restart with

    > ~;container:start; container:reload /

For a web server you need to provide file /etc/meetup.conf with a content

    smtpUser = ""

    smtpHost = smtp.gmail.com
    smtpSender = ""
    smtpPwd = ""

    apiKey = ""

Location of this file can be changed at ./sbt with the value of a property -Dconfig.file