package com.example.vjutest.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true để gửi dưới dạng HTML nếu cần
            javaMailSender.send(mimeMessage);
        } catch (MailException | jakarta.mail.MessagingException e) {
            e.printStackTrace();
        }
    }
}
