package com.aeg.partner;

/**
 * Created by bszucs on 4/6/2016.
 */
public class FileMapping {
    private String local;
    private String remote;
    private String pattern = "*.xml";

    public static FileMapping create(String local, String remote) {
        return new FileMapping(local, remote);
    }

    public FileMapping() {
    }

    private FileMapping(String local, String remote) {
        this.local = local;
        this.remote = remote;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getLocal() {
        return local;
    }

    public String getRemote() {
        return remote;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
