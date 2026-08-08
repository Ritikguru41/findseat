package com.findseat.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends OTP and booking confirmation emails using Gmail SMTP.
 * Callers must catch exceptions — email is never fatal to the request.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String name, String otp) throws Exception {
        String html = """
            <div style="font-family:Arial,sans-serif;background:#0f0f0f;color:#fff;padding:40px;max-width:600px;margin:auto;border-radius:12px;">
              <div style="text-align:center;margin-bottom:30px;">
                <h1 style="color:#e50914;font-size:32px;margin:0;">\uD83C\uDFAC FindSeat</h1>
                <p style="color:#aaa;margin-top:8px;">Your premium movie booking platform</p>
              </div>
              <h2 style="color:#fff;">Hello, %s! \uD83D\uDC4B</h2>
              <p style="color:#ccc;line-height:1.6;">Your OTP is <strong style="font-size:24px;color:#4ade80;">%s</strong>. It will expire in 2 minutes.</p>
              <p style="color:#888;font-size:13px;">If you didn't request this, please ignore this email.</p>
              <hr style="border-color:#333;margin:20px 0;">
              <p style="color:#555;font-size:12px;text-align:center;">\u00A9 2024 FindSeat. All rights reserved.</p>
            </div>
            """.formatted(name, otp);

        sendHtml(to, "\uD83C\uDFAC Your FindSeat OTP Verification Code", html, null);
    }

    public void sendBookingConfirmationEmail(String to, String name, String movieTitle,
                                             String showDate, String showTime, String seats,
                                             String totalAmount, String bookingId, byte[] pdfBytes) throws Exception {
        String html = """
            <div style="font-family:Arial,sans-serif;background:#0f0f0f;color:#fff;padding:40px;max-width:600px;margin:auto;border-radius:12px;">
              <div style="text-align:center;margin-bottom:30px;">
                <h1 style="color:#e50914;font-size:32px;margin:0;">\uD83C\uDFAC FindSeat</h1>
              </div>
              <div style="background:#1a1a1a;border-radius:10px;padding:24px;margin-bottom:20px;">
                <h2 style="color:#e50914;margin-top:0;">\uD83C\uDF9F\uFE0F Booking Confirmed!</h2>
                <p style="color:#ccc;">Hi %s, your booking is confirmed. Enjoy the movie!</p>
                <table style="width:100%%;border-collapse:collapse;margin-top:16px;">
                  <tr><td style="color:#888;padding:6px 0;">Movie</td><td style="color:#fff;font-weight:bold;">%s</td></tr>
                  <tr><td style="color:#888;padding:6px 0;">Date &amp; Time</td><td style="color:#fff;">%s at %s</td></tr>
                  <tr><td style="color:#888;padding:6px 0;">Seats</td><td style="color:#e50914;font-weight:bold;">%s</td></tr>
                  <tr><td style="color:#888;padding:6px 0;">Amount Paid</td><td style="color:#4ade80;font-weight:bold;">\u20B9%s</td></tr>
                  <tr><td style="color:#888;padding:6px 0;">Booking ID</td><td style="color:#fff;font-family:monospace;">%s</td></tr>
                </table>
              </div>
              <p style="color:#aaa;">Your PDF ticket is attached. Present it at the cinema entrance.</p>
              <hr style="border-color:#333;margin:20px 0;">
              <p style="color:#555;font-size:12px;text-align:center;">\u00A9 2024 FindSeat. All rights reserved.</p>
            </div>
            """.formatted(name, movieTitle, showDate, showTime, seats, totalAmount, bookingId);

        sendHtml(to, "\uD83C\uDF9F\uFE0F Booking Confirmed - " + movieTitle + " | FindSeat", html, pdfBytes);
    }

    private void sendHtml(String to, String subject, String html, byte[] attachment) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        if (attachment != null) {
            helper.addAttachment("FindSeat_Ticket.pdf", () -> new java.io.ByteArrayInputStream(attachment));
        }
        mailSender.send(message);
        log.info("Email sent to {}", to);
    }
}
