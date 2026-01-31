package com.riteek.store.email.service;

import com.riteek.store.exceptions.CustomExceptions.ExternalServiceException;
import com.riteek.store.exceptions.CustomExceptions.ServiceDownException;
import com.riteek.store.exceptions.types.ErrorCodes;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private final SendGrid sendGrid;

    public EmailService(@Value("${sendgrid.api-key}") String apiKey) {
        this.sendGrid = new SendGrid(apiKey);
    }

    public void sendOtpEmail(String toEmail, String otp) {

        Email from = new Email("riteek.codes@gmail.com");
        Email to = new Email(toEmail);
        String subject = "Your OTP Code";

        Content content = new Content(
                "text/html",
                "<h2>Your OTP is " + otp + "</h2><br>"
        );

        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                throw new ExternalServiceException(ErrorCodes.EMAIL_REQUEST_FAILED, ErrorCodes.EMAIL_REQUEST_FAILED.getDefaultMessage());
            }

        } catch (IOException ex) {
            throw new ServiceDownException(ErrorCodes.EMAIL_SERVICE_UNAVAILABLE, ErrorCodes.EMAIL_SERVICE_UNAVAILABLE.getDefaultMessage(), ex);
        }
    }
}
