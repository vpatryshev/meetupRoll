set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_31
set JAVA_OPTS=-XX:MaxPermSize=128m

rem java -cp target/MeetupRoll-assembly-0.3.jar com.micronautics.meetupRoll.MeetupRoll
rem start /b javaw -cp target/MeetupRoll-assembly-0.3.jar com.micronautics.meetupRoll.MeetupRoll
"%JAVA_HOME%\bin\java" %JAVA_OPTS% -Xmx512M -jar c:/storage/programming/scala/sbt-launch.jar "run-main com.micronautics.meetupRoll.MeetupRoll"
