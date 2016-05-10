package com.aeg.mail;

/**
 * Created by bszucs on 5/4/2016.
 */
public class TransferFile {

    private String fileName;
    private String dir;
    private boolean remote;
    private boolean encryped;

    private boolean failed = false;
    private String reason;
    private Throwable exception;


}
