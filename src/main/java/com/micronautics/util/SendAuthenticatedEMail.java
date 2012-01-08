package com.micronautics.util;

/** Send authenticated SMTP email. Properties must be specified in meetup.properties
 * <pre>
 * smtpHost = smtp.googlemail.com
 * smtpUser = user@gmail.com
 * smtpPwd  = myPass
 * smtpPort = 465
 * </pre>
 * @see http://javamail.kenai.com/nonav/javadocs/javax/mail/package-summary.html
 * @author Sudhir Ancha
 * @author Mike Slinn */

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class SendAuthenticatedEMail {
	private String smtpHost;
	private String smtpUser;
	private String smtpPwd;
	private int smtpPort;


	public static void main(String args[]) throws Exception {
		SendAuthenticatedEMail smtpMailSender = new SendAuthenticatedEMail();
		System.out.println("Sending mail ...");
		smtpMailSender.sendMail("mslinn@gmail.com", "Test Subject", "Test message.", "mslinn@gmail.com");
		System.out.println("\nDone.");
	}

	public static void sendEmail(String recipients, String subject, String body, String from) {
		try {
			new SendAuthenticatedEMail().sendMail(recipients, subject, body, from);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMail(String recipients, String subject, String body, String from) throws MessagingException {
		readProps();

		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable","true");
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

	private void readProps() {
		Properties properties = new Properties();
		InputStream in = getClass().getClassLoader().getResourceAsStream(
				"meetup.properties");
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

	/** Does simple authentication when the SMTP server requires it. */
	private class SMTPAuthenticator extends javax.mail.Authenticator {

		public PasswordAuthentication getPasswordAuthentication() {
			String username = smtpUser;
			String password = smtpPwd;
			return new PasswordAuthentication(username, password);
		}
	}
}
