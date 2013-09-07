package com.micronautics.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class SendAuthenticatedEMailTest {
    public static void main(String args[]) throws Exception {
        String guineaPig = "vpatryshev@gmail.com";
        System.out.println("Sending mail ...");
        Config config = ConfigFactory.load("meetup.conf");
        new Mailer().sendMail(
                guineaPig, config.getString("smtpHost"), config.getString("smtpPwd"), "Test Subject", "Test message.");
        System.out.println("\nDone.");
    }
}
