package com.aeg.mail;

import com.aeg.util.JodaUtil;
import org.joda.time.DateTime;

import java.net.UnknownHostException;

/**
 * Created by bszucs on 5/4/2016.
 */
public class MessageFactory {

    public static Message createInfo(String message) {
        return new Message(MessageType.INFO, message);
    }

    public static Message createInfo(DateTime startedAt) {
        String server = "",ip = "";
        try {
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            localMachine.getHostName();
            ip = localMachine.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String msg = String.format("Started processing files at %s on machine %s with IP %s", JodaUtil.formatDateTime(startedAt),server, ip);
        return new Message(MessageType.INFO, msg);
    }
}
