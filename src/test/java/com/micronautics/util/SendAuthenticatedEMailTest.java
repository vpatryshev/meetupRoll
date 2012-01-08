package com.micronautics.util;

public class SendAuthenticatedEMailTest {
    public static void main(String args[]) throws Exception {
        System.out.println("Sending mail ...");
        SendAuthenticatedEMail.sendEmail("mslinn@gmail.com", "Test Subject", "Test message.", "mslinn@gmail.com");
        System.out.println("\nDone.");
    }
}
