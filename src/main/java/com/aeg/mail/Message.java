package com.aeg.mail;

import com.aeg.util.JodaUtil;
import org.joda.time.DateTime;

import java.net.UnknownHostException;

/**
 * Created by bszucs on 5/4/2016.
 */
public class Message {

    private MessageType type;
    private String message;

    public Message(){}

    public Message(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
