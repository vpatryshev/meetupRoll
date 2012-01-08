package com.micronautics.util;

/** Mike Slinn modified http://www.javacommerce.com/displaypage.jsp?name=javamail.sql&id=18274
 * Send authenticated SMTP email. Properties must be specified in meetup.properties
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
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMailUsingAuthentication {
	private String smtpHost;
	private String smtpUser;
	private String smtpPwd;
	private int smtpPort;

	private static final String emailMsgTxt = "Test message.";
	private static final String emailSubjectTxt = "Test Subject";
	private static final String emailFromAddress = "mslinn@micronauticsresearch.com";

	/** List of Email addresses to whom email needs to be sent */
	private static final String[] emailList = { emailFromAddress };

	public static void main(String args[]) throws Exception {
		SendMailUsingAuthentication smtpMailSender = new SendMailUsingAuthentication();
		System.out.println("Sending mail ...");
		smtpMailSender.postMail(emailList, emailSubjectTxt, emailMsgTxt,
				emailFromAddress);
		System.out.println("\nDone.");
	}

	public void postMail(String recipients[], String subject, String message,
			String from) throws MessagingException {
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
