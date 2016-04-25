package com.aeg.partner;


import java.util.ArrayList;
import java.util.Collection;

public class Partner {

    private String name;
    private Boolean ftp = false;
    private String keyFile = "";
    private String keyUser = "";
    private String keyPassword = "";
    private Boolean encrypted = false;
    private String host;
    private int port;
    private String username;
    private String password;
    private Collection<FileMapping> inboundFileMappings = new ArrayList<FileMapping>();
    private Collection<FileMapping> outboundFileMappings = new ArrayList<FileMapping>();

    public void setName(String name) {
        this.name = name;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void addInboundMapping(String local, String remote) {
        inboundFileMappings.add(FileMapping.create(local, remote));
    }

    public void addOutboundMapping(String local, String remote) {
        outboundFileMappings.add(FileMapping.create(local, remote));
    }

    public String getName() {
        return name;
    }

    public Collection<FileMapping> getInboundFileMappings() {
        return inboundFileMappings;
    }

    public Collection<FileMapping> getOutboundFileMappings() {
        return outboundFileMappings;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setInboundFileMappings(Collection<FileMapping> fileMappings) {
        this.inboundFileMappings = fileMappings;
    }

    public void setOutboundFileMappings(Collection<FileMapping> fileMappings) {
        this.outboundFileMappings = fileMappings;
    }

    public void setFtp(Boolean ftp) {
        this.ftp = ftp;
    }

    public Boolean getFtp() {
        return ftp;
    }

    public Boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void setKeyUser(String keyUser) {
        this.keyUser = keyUser;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public String getKeyUser() {
        return keyUser;
    }
}
