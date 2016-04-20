package com.aeg.transfer;

/**
 * Created by bszucs on 4/10/2016.
 */
public class Connection {
    private String host;
    private int port = 22;
    private String username;
    private String password;

    public Connection() {
    }

    public Connection(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = 22;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
