package com.aeg.mail;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MailMan {
    private static MailMan INSTANCE = null;

    private Logger log = LogManager.getLogger(MailMan.class.getName());

    public static void deliver() {
        getInstance().internalDeliver();
    }

    private MailMan() {
    }

    private static MailMan getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new MailMan();
        }
        return INSTANCE;
    }

    private void internalDeliver() {


        final String username = "brian@zooxmusic.com";
        final String password = "Z00xMu$1c";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("transfers@ims.com"));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("bszucs@ameresco.com"));
            message.setSubject("IMS Transfer Files Failed");
            message.setText("There was an error transferring the IMS files (need to add the code to specifically state it here)");

            Transport.send(message);

            log.info("Mail sent successfully");


        } catch (MessagingException e) {
            log.error(e.getMessage(), e);

            throw new RuntimeException(e);
        }
    }


}
