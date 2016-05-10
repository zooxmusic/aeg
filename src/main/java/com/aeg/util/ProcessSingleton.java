package com.aeg.util;

import com.aeg.mail.*;
import com.aeg.partner.Partner;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bszucs on 5/4/2016.
 */
public class ProcessSingleton {
    private DateTime startedAt;
    private DateTime endedAt;
    private PartnerTransfer current = null;
    private List<Message> message = new ArrayList<Message>();
    private List<PartnerTransfer> partners = new ArrayList<PartnerTransfer>();

    private static ProcessSingleton INSTANCE = null;

    private static ThreadLocal<ProcessSingleton> THREAD_LOCAL = new ThreadLocal() {
        @Override
        protected ProcessSingleton initialValue() {
            return new ProcessSingleton();
        }
    };

    private ProcessSingleton() {
        this.startedAt = DateTime.now();
    }

    // Static getter
    public static ProcessSingleton getInstance() {
        return THREAD_LOCAL.get();
    }

    private void addCurrent(Partner pt) {
        getInstance().current = PartnerTransfer.create(pt);
        getInstance().partners.add(current);
    }

    public void start() {
        getInstance().message.add(MessageFactory.createInfo(DateTime.now()));
    }

    public PartnerTransfer getCurrent() {
        return getInstance().current;
    }

    public void deliverMail() {
        boolean debug = false;
        if(!debug) return;
        StringBuffer buffer = new StringBuffer();
        for(Message msg : getInstance().message) {
            buffer.append(msg.getMessage());
            buffer.append("<br/>");
        }
        MailMan.deliver("Aggregated Info", buffer.toString());
    }
}
