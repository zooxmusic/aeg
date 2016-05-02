package com.aeg.transfer;

import com.aeg.mail.MailMan;
import com.aeg.partner.FileMapping;
import com.aeg.partner.Partner;
import com.aeg.pgp.PGPFileProcessor;
import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by bszucs on 4/10/2016.
 */
public class SftpTransferService extends AbstractTransferService {

    private Logger log = LogManager.getLogger(SftpTransferService.class.getName());

    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;

    public static SftpTransferService create() {
        return new SftpTransferService();
    }

    protected void outbound(Vector<File> filesToTransfer, File localDir, File processedDir, String remoteDir, Partner partner) throws Exception {

        Vector<File> filesToMove = new Vector<File>();

        channelSftp.cd(remoteDir);

        for (File file : filesToTransfer) {

            FileInputStream fis = new FileInputStream(file);

            // copy to their server
            channelSftp.put(fis, file.getName());

            fis.close();

            log.info(String.format("File %s transferred successfully to host.", file.getName()));

            filesToMove.add(file);
        }

        // move them all to processed
        moveFilesToProcessed(localDir, filesToMove);

        // maybe delete the originals

    }


    // inbound means "downloading". We always look from our perspective
    protected Vector<String> inbound(final String remoteDir, final String filter, final File localDir, final Partner partner) throws SftpException, FileNotFoundException, JSchException {

        String localFilePath = "";
        Vector<String> processedLocalFiles = new Vector<String>();

        try {
            channelSftp.cd(remoteDir);

            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(filter);

            for (ChannelSftp.LsEntry file : files) {
                try {

                    localFilePath = String.format("%s/%s", localDir.getAbsolutePath(), file.getFilename());

                    channelSftp.get(file.getFilename(), localDir.getAbsolutePath());

                    String message = String.format("Remote File: [%s] was downloaded to %s", file.getFilename(), localFilePath);
                    log.info(message);

                    String oldPath = String.format("%s/%s", remoteDir, file.getFilename());
                    String newPath = String.format("%s/done/%s", remoteDir, file.getFilename());
                    channelSftp.rename(oldPath, newPath);

                    processedLocalFiles.add(localFilePath);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    //continue;
                }



            }


        } catch (Exception e) {

            throw e;
        } finally {
            cleanup();
        }
        return processedLocalFiles;
    }

    protected void connect(final Connection connection) throws Exception {
        log.info("Connecting to sftp...");

        JSch jsch = new JSch();
        session = jsch.getSession(connection.getUsername(), connection.getHost(), connection.getPort());
        session.setPassword(connection.getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        log.info("Host connected.");
        channel = session.openChannel("sftp");
        channel.connect();

        log.info("sftp channel opened and connected.");
        channelSftp = (ChannelSftp) channel;

    }

    protected void cleanup() {
        if (null != channelSftp) {
            channelSftp.exit();
            log.info("sftp Channel exited.");
        }
        if (null != channel) {
            channel.disconnect();
            log.info("Channel disconnected.");
        }
        if (null != session) {
            session.disconnect();
            log.info("Host Session disconnected.");
        }
    }

}
