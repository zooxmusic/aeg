package com.aeg.transfer.saved;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by bszucs on 4/10/2016.
 */
public class SSHManagerTester {


    public static void main(String[] args) {

        String f = "C:/TestUpload.xml";

        System.out.println("sendCommand");

        /**
         * YOU MUST CHANGE THE FOLLOWING
         * FILE_NAME: A FILE IN THE DIRECTORY
         * USER: LOGIN USER NAME
         * PASSWORD: PASSWORD FOR THAT USER
         * HOST: IP ADDRESS OF THE SSH SERVER
         **/
        String command = "ls /HVAC - Files For Testing/";

        String userName = "bszucs";
        String password = "109F0r3$t";
        String connectionIP = "vault.clearesult.com";
        SSHManager instance = new SSHManager(userName, password, connectionIP, "");
        String errorMessage = instance.connect();

        if (errorMessage != null) {
            System.out.println(errorMessage);
            System.err.println(errorMessage);
        }

        String expResult = "FILE_NAME\n";
        // call sendCommand for each command and the output
        //(without prompts) is returned
        String from = "/HVAC - Files For Testing/";
        String to = "C:/AEG/TESTING/inbound/";
        File dir = new File(to);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String result = instance.downloadAll(from, "*.xml", to);
        //String result = instance.sendCommand(command);
        // close only after all commands are sent


        instance.close();

    }

    public static void send(String fileName) {
        String SFTPHOST = "vault.clearesult.com";
        int SFTPPORT = 22;
        String SFTPUSER = "bszucs";
        String SFTPPASS = "109F0r3$t";
        String SFTPWORKINGDIR = "/HVAC - Files For Testing/";

        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        System.out.println("preparing the host information for sftp.");
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            System.out.println("Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
            System.out.println("sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(SFTPWORKINGDIR);
            File f = new File(fileName);
            channelSftp.put(new FileInputStream(f), f.getName());
            //log.info("File transfered successfully to host.");
        } catch (Exception ex) {
            System.out.println("Exception found while tranfer the response.");
        } finally {

            channelSftp.exit();
            System.out.println("sftp Channel exited.");
            channel.disconnect();
            System.out.println("Channel disconnected.");
            session.disconnect();
            System.out.println("Host Session disconnected.");
        }
    }
}
