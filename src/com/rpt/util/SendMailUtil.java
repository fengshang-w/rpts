package com.rpt.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.synnex.cdc.waf.util.Log;

public class SendMailUtil {
	private static String host = "mail.ndscd.com";
	private static String user = "rptmail";
	private static String pwd = "sbpcd-it";
	private static int port = 25;

	public static void sendMail(String sendFrom, String sendTo, String sendCC, String sendBCC,
			String subject, String content) {
		try {
			Log.log("SendMail:from:" + sendFrom + " to:" + sendTo + " subject:"
					+ subject);
			Properties props = System.getProperties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
	        Authenticator auth = new Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(user, pwd);
	            }
	        };
			Session session;
			session = Session.getDefaultInstance(props, auth);
			// session.setDebug(true);
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(sendFrom));

			InternetAddress[] address = InternetAddress.parse(sendTo);
			MimeMultipart multi = new MimeMultipart();
			BodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setContent(content, "text/html;charset=utf-8");
			multi.addBodyPart(textBodyPart);
			msg.setRecipients(Message.RecipientType.TO, address);

			if (sendCC != null && !sendCC.trim().equals("")) {
				InternetAddress[] addressCC = InternetAddress.parse(sendCC);
				msg.setRecipients(Message.RecipientType.CC, addressCC);
			}
			
			if (sendBCC != null && !sendBCC.trim().equals("")) {
				InternetAddress[] addressBCC = InternetAddress.parse(sendBCC);
				msg.setRecipients(Message.RecipientType.BCC, addressBCC);
			}

			msg.setSubject(subject);
			msg.setContent(multi);
			Transport.send(msg);

			Log.log("SendMail:from:" + sendFrom + " to:" + sendTo + " successed.");
		} catch (MessagingException mex) {
			Log.logErr(mex);
		}
	}
}
