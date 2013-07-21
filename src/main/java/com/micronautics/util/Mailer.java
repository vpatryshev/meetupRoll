package com.micronautics.util;

/** Send authenticated SMTP email. MeetupRoll.props must be specified in meetup.MeetupRoll.props
 * <pre>
 * smtpHost = smtp.googlemail.com
 * smtpUser = user@gmail.com
 * smtpPwd  = myPass
 * smtpPort = 465
 * </pre>
 * @see http://javamail.kenai.com/nonav/javadocs/javax/mail/package-summary.html
 * @author Sudhir Ancha
 * @author Mike Slinn */

/* Copyright 1012 Micronautics Research Corporation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Subject to the additional condition that the attribution code in Gui.scala
   remains untouched and displays each time the program runs.

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

import com.typesafe.config.Config;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class Mailer {
    private Config config;

    public Mailer(Config config) {
        this.config = config;
    }
    private boolean dryRun = true;
    private String smtpHost;
    private String smtpUser;
    private String smtpPwd;
    private int smtpPort;

    private static void log(String s) { System.out.println(s); }

    public void sendMail(String from, String recipients, String subject, String body) throws MessagingException {
        if (dryRun) {
            log("From: " + smtpUser);
            log("To: " + recipients);
            log("Subj: " + subject);
            log("\n---------------------------\n" + body + "\n----------------------\n");
            return;
        }
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", smtpPort);

        Authenticator auth = new SMTPAuthenticator();
        Session session = Session.getDefaultInstance(props, auth);
        session.setDebug(false);

        Message msg = new MimeMessage(session);
        final Address[] recipientAddresses = InternetAddress.parse(recipients);
        msg.setRecipients(Message.RecipientType.TO, recipientAddresses);
        msg.setSentDate(new Date());
        msg.setSubject(subject);
        msg.setText(body);
        msg.setSubject(subject);
        msg.setText(body);

        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);

        Transport transport = session.getTransport("smtps");
        try {
            transport.connect(smtpHost, smtpPort, smtpUser, smtpPwd);
            transport.sendMessage(msg, recipientAddresses);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            transport.close();
        }
    }

    /**
     * Does simple authentication when the SMTP server requires it.
     */
    private class SMTPAuthenticator extends javax.mail.Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
            String username = smtpUser;
            String password = smtpPwd;
            return new PasswordAuthentication(username, password);
        }
    }
}