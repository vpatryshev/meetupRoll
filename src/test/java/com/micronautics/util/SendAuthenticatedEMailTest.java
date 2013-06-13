package com.micronautics.util;

import com.typesafe.config.ConfigFactory;

public class SendAuthenticatedEMailTest {
    public static void main(String args[]) throws Exception {
        String guineaPig = "vpatryshev@gmail.com";
        System.out.println("Sending mail ...");
        new Mailer(ConfigFactory.load("meetup.conf")).sendMail(guineaPig, guineaPig, "Test Subject", "Test message.");
        System.out.println("\nDone.");
    }
}
