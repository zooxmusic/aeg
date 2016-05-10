package com.aeg.mail;

import java.util.HashSet;
import java.util.Set;

public class EmailMessage {

    private String content;
    private String mimeType;  // optional
    private Set<PartnerTransfer> transfers = new HashSet<PartnerTransfer>();

    private EmailMessage() {}

    public interface IContent {
        IBuild content(String content);
    }

    public interface IBuild {
        IBuild mimeType(String mimeTypeName);

        EmailMessage build();
    }

    private static class Builder implements IContent, IBuild {
        private EmailMessage instance = new EmailMessage();

        @Override
        public IBuild content(String content) {
            instance.content = content;
            return this;
        }

        @Override
        public IBuild mimeType(String mimeTypeName) {
            instance.mimeType = mimeTypeName;
            return this;
        }

        @Override
        public EmailMessage build() {
            return instance;
        }
    }
}