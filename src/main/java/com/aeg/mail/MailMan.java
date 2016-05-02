package com.aeg.mail;


import com.aeg.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MailMan {
    private static MailMan INSTANCE = null;

    private String mailPropertiesPath = "%s/config/mail.properties";
    private String username;
    private String password;
    private String host;
    private Integer port = null;
    private String auth;
    private String startTlsEnable;
    private String from = "ims-transfer@Ameresco.com";
    private String to;
    private String testTo;

    private Logger log = LogManager.getLogger(MailMan.class.getName());

    public static void deliver(final String subject, final String message) {
        getInstance().internalDeliver(subject, message, false);
    }

    public static void deliverTest() {
        getInstance().internalDeliver("Test Email", "Test Email", true);
    }
    public static void deliverException(final String message) {
        getInstance().internalDeliver("Exception", message, false);
    }

    public static void deliverNewFiles(final String message) {
        getInstance().internalDeliver("New Files", message, false);
    }


    private MailMan() {
        loadProperties();
    }

    private void loadProperties() {
        Properties properties = new Properties();
        try {

            String aegHome = System.getenv().get("AEG_HOME");
            if(null == aegHome || "".equalsIgnoreCase(aegHome)) {
                throw new Exception("AEG_HOME is not set");
            }

            String path = String.format(mailPropertiesPath, aegHome);
            InputStream inputStream = new FileInputStream(path);
            // if there is no global properties then load the local one
            if(null == inputStream) {
                inputStream = getClass().getResourceAsStream("/mail.properties");
            }
            properties.load(inputStream);
            inputStream.close();
        }catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        if(properties.containsKey("mail.smtp.username")) {
            username = properties.getProperty("mail.smtp.username");
        }
        if(properties.containsKey("mail.smtp.password")) {
            password = properties.getProperty("mail.smtp.password");
        }
        if(properties.containsKey("mail.smtp.host")) {
            host = properties.getProperty("mail.smtp.host");
        }
        if(properties.containsKey("mail.smtp.auth")) {
            auth = properties.getProperty("mail.smtp.auth");
        }
        if(properties.containsKey("mail.smtp.starttls.enable")) {
            startTlsEnable = properties.getProperty("mail.smtp.starttls.enable");
        }
        if(properties.containsKey("mail.smtp.port")) {
            String tmpPort = properties.getProperty("mail.smtp.port");
            if(!"".equalsIgnoreCase(tmpPort)) {
                port = Integer.getInteger(tmpPort);
            }
        }
        if(properties.containsKey("mail.smtp.from")) {
            from = properties.getProperty("mail.smtp.from");
        }
        if(properties.containsKey("mail.smtp.to")) {
            to = properties.getProperty("mail.smtp.to");
        }
        if(properties.containsKey("mail.smtp.test.to")) {
            testTo = properties.getProperty("mail.smtp.test.to");
        }

    }
    private static MailMan getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new MailMan();
        }
        return INSTANCE;
    }

    private void internalDeliver(final String subject, final String msg, final boolean test) {

        try {

            Address[] recipients = (test)? getTestRecipients() : getRecipients();

            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, recipients);
            message.setSubject(subject);
            message.setText(msg);

            Transport.send(message);

            log.info("Mail sent successfully");


        } catch (MessagingException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
    }

    private Address[] getRecipients() throws AddressException {
        //return InternetAddress.parse("bszucs@ameresco.com,nmenon@appliedEnergyGroup.com,BMurnane@appliedenergygroup.com");
        return InternetAddress.parse(to);
    }

    private Address[] getTestRecipients() throws AddressException {
        return InternetAddress.parse(testTo);
    }

    private Properties getProperties() {
        Properties properties = new Properties();

        properties.put("mail.smtp.host", host);
        if(StringUtil.isNotEmpty(auth)) {
            properties.put("mail.smtp.auth", auth);
        }
        if(StringUtil.isNotEmpty(startTlsEnable)) {
            properties.put("mail.smtp.starttls.enable", startTlsEnable);
        }
        if(null != port) {
            properties.put("mail.smtp.port", port);
        }


        return properties;
    }


    private Session getSession() {
        if("true".equalsIgnoreCase(auth)) {
            return Session.getInstance(getProperties(),
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
        }
        return Session.getDefaultInstance(getProperties());
    }

}
