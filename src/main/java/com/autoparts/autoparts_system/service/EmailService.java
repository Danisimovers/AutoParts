package com.autoparts.autoparts_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Подтверждение регистрации - AutoParts Shop";
        String verificationUrl = "http://localhost:5173/verify?token=" + token;
        String text = "Здравствуйте!\n\n"
                + "Вы зарегистрировались в магазине автозапчастей AutoParts Shop.\n\n"
                + "Для подтверждения регистрации перейдите по ссылке:\n\n"
                + verificationUrl + "\n\n"
                + "Ссылка действительна 24 часа.\n\n"
                + "Если вы не регистрировались, проигнорируйте это письмо.\n\n"
                + "С уважением,\n"
                + "Команда AutoParts Shop";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("kazauto1005@mail.ru");

        try {
            mailSender.send(message);
            System.out.println("Verification email sent to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Ошибка отправки письма: " + e.getMessage());
        }
    }


    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Восстановление пароля - AutoParts Shop";
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        String text = "Здравствуйте!\n\n"
                + "Вы запросили восстановление пароля в магазине автозапчастей AutoParts Shop.\n\n"
                + "Для сброса пароля перейдите по ссылке:\n\n"
                + resetUrl + "\n\n"
                + "Ссылка действительна 1 час.\n\n"
                + "Если вы не запрашивали восстановление пароля, проигнорируйте это письмо.\n\n"
                + "С уважением,\n"
                + "Команда AutoParts Shop";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("kazauto1005@mail.ru");

        try {
            mailSender.send(message);
            System.out.println("Password reset email sent to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            throw new RuntimeException("Ошибка отправки письма для восстановления пароля");
        }
    }
}