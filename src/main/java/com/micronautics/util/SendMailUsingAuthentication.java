package com.micronautics.util;
/* From http://www.javacommerce.com/displaypage.jsp?name=javamail.sql&id=18274
Some SMTP servers require a username and password authentication before you
can use their Server for Sending mail. This is most common with couple
of ISP's who provide SMTP Address to Send Mail.

This Program gives any example on how to do SMTP Authentication
(User and Password verification)

This is a free source code and is provided as it is without any warranties and
it can be used in any your code for free.

Author : Sudhir Ancha
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*
 To use this program, change values for the following three constants,

 SMTP_HOST_NAME -- Has your SMTP Host Name
 SMTP_AUTH_USER -- Has your SMTP Authentication UserName
 SMTP_AUTH_PWD  -- Has your SMTP Authentication Password

 Next change values for fields

 emailMsgTxt  -- Message Text for the Email
 emailSubjectTxt  -- Subject for email
 emailFromAddress -- Email Address whose name will appears as "from" address

 Next change value for "emailList".
 This String array has List of all Email Addresses to Email Email needs to be sent to.


 Next to run the program, execute it as follows,

 SendMailUsingAuthentication authProg = new SendMailUsingAuthentication();
 */

public class SendMailUsingAuthentication {
	private String smtpHost;
	private String smtpUser;
	private String smtpPwd;
	private int smtpPort;

	private static final String emailMsgTxt = "Test message.";
	private static final String emailSubjectTxt = "Test Subject";
	private static final String emailFromAddress = "mslinn@micronauticsresearch.com";

	/** List of Email addresses to who email needs to be sent to */
	private static final String[] emailList = { emailFromAddress };

	public static void main(String args[]) throws Exception {
		SendMailUsingAuthentication smtpMailSender = new SendMailUsingAuthentication();
		System.out.println("Sending mail ...");
		smtpMailSender.postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress);
		System.out.println("\nDone.");
	}

	public void postMail(String recipients[], String subject, String message, String from) throws MessagingException {
		boolean debug = false;

		readProps();

		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", smtpPort);

		Authenticator auth = new SMTPAuthenticator();
		Session session = Session.getDefaultInstance(props, auth);
		session.setDebug(debug);

		Message msg = new MimeMessage(session);
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);

		InternetAddress[] addressTo = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i++)
			addressTo[i] = new InternetAddress(recipients[i]);
		msg.setRecipients(Message.RecipientType.TO, addressTo);

		msg.setSubject(subject);
		msg.setContent(message, "text/plain");
		try {
		    Transport.send(msg);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	private void readProps() {
		Properties properties = new Properties();
		InputStream in = getClass().getClassLoader().getResourceAsStream("meetup.properties");
		if (in != null) {
			try {
				properties.load(in);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			smtpHost = properties.getProperty("smtpHost");
			smtpPort = Integer.parseInt(properties.getProperty("smtpPort"));
			smtpUser = properties.getProperty("smtpUser");
			smtpPwd = properties.getProperty("smtpPwd");
		}
	}

	/**
	 * SimpleAuthenticator is used to do simple authentication when the SMTP server requires it. */
	private class SMTPAuthenticator extends javax.mail.Authenticator {

		public PasswordAuthentication getPasswordAuthentication() {
			String username = smtpUser;
			String password = smtpPwd;
			return new PasswordAuthentication(username, password);
		}
	}

}
