package com.rpt.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.synnex.cdc.waf.util.Log;

public class MailUtil {
	
	private Properties mailP = null;
	private static MailUtil instanse;
	public MailUtil(){
		mailP = new Properties();
		InputStream in = MailUtil.class.getResourceAsStream("/mail.properties");
		try {
			mailP.load(in);
		} catch (IOException e) {
		}
	}
		
	public static MailUtil getInstanse() {
		if (instanse == null) {
			synchronized (MailUtil.class) {
				if (instanse == null) {
					instanse = new MailUtil();
				}
			}
		}
		return instanse;
	}

	public void sendMailN(String sendTo, String sendCC, String sendBCC,
			String subject, String content) {
		String sendFrom = mailP.getProperty("mail.smtp.from");
		Log.log("SendMail:from:" + sendFrom + " to:" + sendTo + " cc:" + sendCC + " subject:"
				+ subject);
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", mailP.getProperty("mail.smtp.host"));
			props.put("mail.smtp.port", mailP.getProperty("mail.smtp.port"));
			Authenticator auth = null;
//	        Authenticator auth = new Authenticator() {
//	            @Override
//	            protected PasswordAuthentication getPasswordAuthentication() {
//	                return new PasswordAuthentication("mail.smtp.user", "mail.smtp.pwd");
//	            }
//	        };
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

			Log.log("SendMail:from:" + sendFrom + " to:" + sendTo + " cc:" + sendCC + " successed.");
		} catch (Exception e) {
			Log.log("SendMail Error: from:" + sendFrom + " to:" + sendTo + " cc:" + sendCC + " "+e);
		}
	}
	
	public static boolean isNull(String str) {
		if (str == null || "".equals(str)) {
			return true;
		}
		return false;
	}
}
