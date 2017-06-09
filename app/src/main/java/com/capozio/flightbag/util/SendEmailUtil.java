package com.capozio.flightbag.util;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/*** ***********************************************************************
 * <p>
 * Pilot Training System CONFIDENTIAL
 * __________________
 * <p>
 * [2015] - [2017] Pilot Training System
 * All Rights Reserved.
 * <p>
 * NOTICE:  All information contained herein is, and remains
 * the property of Pilot Training System,
 * The intellectual and technical concepts contained
 * herein are proprietary to Pilot Training System
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Pilot Training System.
 *
 * Created by Ying Zhang on 12/2/16.
 */

/**
 *  Manages the sending of emails.
 */

public class SendEmailUtil {
    public static  boolean sendEmail(String email, File attachment) throws MessagingException {
        //Creating properties
        Properties props = new Properties();

        //Configuring properties for gmail
        //If you are not using gmail you may need to change the values
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        //Creating a new session
        //Set the credentials here
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    //Authenticating the password
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(Configs.EMAIL, Configs.PASSWORD);
                    }
                });

        // Debug mode
        //session.setDebug(true);
        //Creating MimeMessage object
        MimeMessage mm = new MimeMessage(session);
//        try {
        //Setting sender address
        mm.setFrom(new InternetAddress(Configs.EMAIL));
        //Adding receiver
        mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email.isEmpty() ? Configs.BCC_EMAIL : email));
        //Adding subject
        mm.setSubject(Configs.DEFAULT_SUBJECT);

        // Adding cc
        if (!email.isEmpty())
            mm.addRecipient(Message.RecipientType.BCC, new InternetAddress(Configs.BCC_EMAIL));

        //Adding message without attachemnts
//            mm.setText(message);

        // Create the message part
        BodyPart msgBodyPart = new MimeBodyPart();
        msgBodyPart.setText(Configs.DEFAULT_MSG);

        // Create a multipart message
        Multipart multipart = new MimeMultipart();

        // part 1: Set text message part
        multipart.addBodyPart(msgBodyPart);

        // part 2: the attachment
        msgBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(attachment);
        msgBodyPart.setDataHandler(new DataHandler(source));
        msgBodyPart.setFileName(attachment.getName());
        multipart.addBodyPart(msgBodyPart);

        // Send the complete message parts
        mm.setContent(multipart);
        Transport.send(mm);
//        } catch (AddressException e) {
//            e.printStackTrace();
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
        return true;
    }
}
