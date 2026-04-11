package com.example.quizapp.app.utils;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String SENDER_EMAIL    = "gentlefitproject@gmail.com";
    private static final String SENDER_PASSWORD = "ngviqxaijlbhirlj"; // 16-char app password, no spaces

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int    SMTP_PORT = 587;

    public static boolean sendOtpEmail(String toEmail, String otp) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SENDER_EMAIL, "QuizApp"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject("QuizApp – Your Password Reset OTP");
            msg.setContent(buildHtml(otp), "text/html; charset=utf-8");

            Transport.send(msg);
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // check Logcat for exact error
            return false;
        }
    }

    private static String buildHtml(String otp) {
        return "<div style='font-family:sans-serif;max-width:480px;margin:auto'>"
                + "<h2 style='color:#3F51B5'>🎓 QuizApp Password Reset</h2>"
                + "<p>You requested a password reset. Use the code below:</p>"
                + "<div style='background:#F5F5F5;border-radius:8px;padding:24px;text-align:center'>"
                + "<span style='font-size:40px;font-weight:bold;letter-spacing:10px;color:#3F51B5'>"
                + otp + "</span></div>"
                + "<p style='color:#757575;font-size:12px;margin-top:16px'>"
                + "This OTP is valid for this session only. "
                + "If you did not request this, ignore this email.</p></div>";
    }
}
