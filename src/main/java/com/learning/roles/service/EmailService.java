package com.learning.roles.service;


import com.learning.roles.constant.EmailConstants;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

@Service
public class EmailService {


    public void sendNewPassowrdEmail(String firstName, String password, String email) throws MessagingException {
        Message message = this.createEmail(firstName, password, email);
        Transport smtpTransport = getEmailSession().getTransport(EmailConstants.SIMPLE_MAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(EmailConstants.GMAIL_STMP_SERVER, EmailConstants.USERNAME, EmailConstants.PASSWORD);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
    }

    private Message createEmail(String firstName, String password, String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(EmailConstants.FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC,
                InternetAddress.parse(EmailConstants.CC_EMAIL, false));
        message.setSubject(EmailConstants.EMAIL_SUBJECT);
        message.setText("Hello " + firstName + ", \n \n Your new account password is: " + password + "\n \n The support team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }


    private Session getEmailSession(){
        Properties properties = System.getProperties();
        properties.put(EmailConstants.SMTP_HOST, EmailConstants.GMAIL_STMP_SERVER );
        properties.put(EmailConstants.SMTP_AUTH, true);
        properties.put(EmailConstants.SMTP_PORT, EmailConstants.DEFAULT_PORT);
        properties.put(EmailConstants.SMTP_STARTTLS_REQUIRED, true );
        properties.put(EmailConstants.SMTP_STARTTLS_ENABLE, true );
        return Session.getInstance(properties);
    }

}
