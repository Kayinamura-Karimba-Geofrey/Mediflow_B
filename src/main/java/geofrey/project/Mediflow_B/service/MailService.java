package geofrey.project.Mediflow_B.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mediflow Account Verification OTP");
        message.setText("Dear User,\n\n" +
                "Thank you for registering with Mediflow. Your OTP for account verification is: " + otp + "\n\n" +
                "This OTP will expire in 5 minutes.\n\n" +
                "Best regards,\n" +
                "Mediflow Team");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }
}
