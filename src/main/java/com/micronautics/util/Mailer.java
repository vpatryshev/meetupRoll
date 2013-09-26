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

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class Mailer {
    private static int SMTP_PORT = 465;

    public void sendMail(String email, String smtpHost, String smtpSender, String smtpPwd, String subject, String body)
            throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", SMTP_PORT);

        Authenticator auth = new SMTPAuthenticator(smtpSender, smtpPwd);
        Session session = Session.getDefaultInstance(props, auth);
        session.setDebug(false);

        Message msg = new MimeMessage(session);
        final Address[] recipientAddresses = InternetAddress.parse(email);
        msg.setRecipients(Message.RecipientType.TO, recipientAddresses);
        msg.setSentDate(new Date());
        msg.setSubject(subject);
        msg.setText(body);
        msg.setSubject(subject);
        msg.setText(body);

        InternetAddress addressFrom = new InternetAddress(email);
        msg.setFrom(addressFrom);

        Transport transport = session.getTransport("smtps");
        try {
            transport.connect(smtpHost, SMTP_PORT, smtpSender, smtpPwd);
            transport.sendMessage(msg, recipientAddresses);
        } finally {
            transport.close();
        }
    }

    /**
     * Does simple authentication when the SMTP server requires it.
     */
    private class SMTPAuthenticator extends javax.mail.Authenticator {

        private String smtpUser;
        private String smtpPwd;

        public SMTPAuthenticator(String smtpUser, String smtpPwd) {
            this.smtpUser = smtpUser;
            this.smtpPwd = smtpPwd;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(smtpUser, smtpPwd);
        }
    }
}
