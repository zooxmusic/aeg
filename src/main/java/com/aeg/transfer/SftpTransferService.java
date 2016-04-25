package com.aeg.transfer;

import com.aeg.mail.MailMan;
import com.aeg.partner.FileMapping;
import com.aeg.partner.Partner;
import com.aeg.pgp.PGPFileProcessor;
import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by bszucs on 4/10/2016.
 */
public class SftpTransferService implements TransferService {

    private Logger log = LogManager.getLogger(SftpTransferService.class.getName());

    private static final String COMPLETED_EXPRESSION = "done";

    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;

    public static SftpTransferService create() {
        return new SftpTransferService();
    }

    public SftpTransferService() {
    }

    public void outbound(Partner partner) throws IOException, URISyntaxException {
        log.info(">>>  Outbound...");

        Connection conn = getConnection(partner);

        for (FileMapping fileMapping : partner.getOutboundFileMappings()) {

            String localDir = fileMapping.getLocal();
            String remoteDir = fileMapping.getRemote();
            String pattern = fileMapping.getPattern();

            log.info(String.format(">>>   LOCAL DIR: %s", localDir));
            log.info(String.format(">>>   REMOTE DIR: %s", remoteDir));
            log.info(String.format(">>>   PATTERN: %s", pattern));

            try {
                outbound(conn, localDir, pattern, remoteDir, partner);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                MailMan.deliver();
            }
        }
    }

    private void outbound(final Connection connection, final String localDir, final String filter, final String remoteDir, final Partner partner) throws Exception {

        try {
            log.info(">>>   reading INSIDE outbound: ");
            log.info(String.format(">>>   LOCAL DIR: %s", localDir));
            log.info(String.format(">>>   REMOTE DIR: %s", remoteDir));
            log.info(String.format(">>>   FILTER: %s", filter));

            connect(connection);

            boolean bFilter = (null != filter || "".equalsIgnoreCase(filter.trim()));

            String tmpRemote = remoteDir;
            if (!tmpRemote.endsWith("/")) {
                tmpRemote += "/";
            }
            channelSftp.cd(tmpRemote);

            File dir = new File(localDir);
            if (!dir.exists()) {
                dir.mkdirs();

            }
            String doneD = String.format("%s/%s", localDir, COMPLETED_EXPRESSION);
            File doneDir = new File(doneD);
            if(!doneDir.exists()) {
                doneDir.mkdirs();
            }
            File[] files = null;
            if(bFilter) {

                files = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if (name.endsWith(filter)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

            } else {
                files = dir.listFiles();
            }

            if (null == files) {
                String message = String.format("Local directory [%s] was not found", localDir);
                log.error(message);
                throw new FileNotFoundException(message);
            }
            Vector<String> toRename = new Vector<String>();
            Vector<String> toDelete = new Vector<String>();

            for (File file : files) {
                toDelete.add(file.getAbsolutePath());
                File tmpFile = null;

                if(partner.getEncrypted()) {

                    PGPFileProcessor fileProcessor = new PGPFileProcessor();
                    fileProcessor.setAsciiArmored(true);
                    fileProcessor.setInputFile(file.getAbsolutePath());
                    URL url = SftpTransferService.class.getResource(partner.getKeyFile());
                    String outputFile = String.format("%s.gpg", file.getAbsolutePath());
                    fileProcessor.setOutputFile(outputFile);
                    fileProcessor.setKeyFile(url.getPath());
                    fileProcessor.setPassphrase(partner.getKeyPassword());
                    fileProcessor.encrypt();
                    tmpFile = new File(outputFile);
                } else {
                    tmpFile = new File(file.getAbsolutePath());
                }


                FileInputStream fis = new FileInputStream(tmpFile);
                String fileName = tmpFile.getName();
                channelSftp.put(fis, fileName);
                toRename.add(tmpFile.getAbsolutePath());
                fis.close();

                log.info("File transferred successfully to host.");
            }

            for (String fileToRename : toRename) {
                try {
                    renameAndMove(Paths.get(fileToRename), Paths.get(doneDir.getAbsolutePath()));
                } catch (Exception e) {
                    e.printStackTrace();
                    String message = String.format("Failed to rename local file [%s]", fileToRename);
                    log.error(message);
                }
            }

            for(String fileToDelete : toDelete) {
                try {
                    File del = new File(fileToDelete);
                    del.delete();
                }catch(Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            cleanup();
        }
    }

    private Connection getConnection(Partner partner) {
        Connection connection = new Connection();
        connection.setHost(partner.getHost());
        connection.setPort(partner.getPort());
        connection.setUsername(partner.getUsername());
        connection.setPassword(partner.getPassword());
        return connection;
    }

    private void renameAndMove(Path file, Path doneD) throws IOException {
        //String newName = String.format("%s.%s", file.getFileName().toString(), COMPLETED_EXPRESSION);
        String newName = String.format("/%s/%s", COMPLETED_EXPRESSION, file.getFileName().toString());
        Path dir = file.getParent();
        Path fn = doneD.getFileSystem().getPath(newName);
        Path target = (dir == null) ? fn : dir.resolve(fn);

        //String newName = String.format("%s/%s.%s", COMPLETED_EXPRESSION, file.getFileName().toString(), COMPLETED_EXPRESSION);
        //Path dir = file.getParent();
        //Path fn = file.getFileSystem().getPath(newName);
        //Path target = (dir == null) ? fn : dir.resolve(fn);
        Files.move(file, target);
    }

    public void inbound(Partner partner) throws IOException, URISyntaxException {
        log.info("<<< Inbound...");

        Connection conn = getConnection(partner);

        for (FileMapping fileMapping : partner.getInboundFileMappings()) {

            String localDir = fileMapping.getLocal();
            String remoteDir = fileMapping.getRemote();
            String pattern = fileMapping.getPattern();

            log.info(String.format("<<<  REMOTE DIR: %s", remoteDir));
            log.info(String.format("<<<  LOCAL DIR: %s", localDir));
            log.info(String.format("<<<  PATTERN: %s", pattern));

            try {
                inbound(conn, remoteDir, pattern, localDir, partner);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                MailMan.deliver();
            }
        }

    }

    // inbound means "downloading". We always look from our perspective
    private void inbound(final Connection connection, final String remoteDir, String filter, final String localDir, final Partner partner) throws SftpException, FileNotFoundException, JSchException {
        log.info("<<<  reading INSIDE inbound: ");
        log.info(String.format("<<<  REMOTE DIR: %s", remoteDir));
        log.info(String.format("<<<  LOCAL DIR: %s", localDir));
        log.info(String.format("<<<  FILTER: %s", filter));

        try {
            connect(connection);

            String tmpLocal = localDir;
            File local = new File(tmpLocal);
            if (!local.exists()) {
                local.mkdirs();
            }
            if (!tmpLocal.endsWith("/")) {
                tmpLocal += "/";
            }

            channelSftp.cd(remoteDir);

            Vector<String> toDecrypt = new Vector<String>();

            Vector<ChannelSftp.LsEntry> list = channelSftp.ls(filter);
            for (ChannelSftp.LsEntry entry : list) {
                try {

                    String oldName = entry.getFilename();
                    String oldPath = String.format("%s/%s", remoteDir, oldName);

                    String newName = String.format("%s.%s", oldName, COMPLETED_EXPRESSION);
                    String newPath = String.format("%s/%s/%s", remoteDir, COMPLETED_EXPRESSION, newName);

                    channelSftp.get(oldName, tmpLocal);

                    //channelSftp.rename(oldPath, newPath);


                    String message = String.format("Remote File: [%s] was downloaded to %s%s and then renamed to [%s]", oldName, tmpLocal, oldName, newName);
                    log.info(message);


                    if(partner.getEncrypted()) {
                        String fileName = String.format("%s/%s", localDir, oldName);
                        toDecrypt.add(fileName);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }


                try {
                    for(String toD : toDecrypt) {
                        PGPFileProcessor fileProcessor = new PGPFileProcessor();
                        fileProcessor.setAsciiArmored(false);
                        fileProcessor.setInputFile(toD);
                        URL url = SftpTransferService.class.getResource("/keys/ims-pub.asc");
                        String fName = entry.getLongname();
                        String outputFile = fName.substring(0, fName.lastIndexOf("."));
                        fileProcessor.setOutputFile(outputFile);
                        fileProcessor.setKeyFile(url.getPath());
                        fileProcessor.setPassphrase("AEG2016!");
                        fileProcessor.decrypt();
                    }
                }catch(Exception e) {
                    log.error(e.getMessage(), e);
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            cleanup();
        }
    }

    private void connect(final Connection connection) throws FileNotFoundException, JSchException, SftpException {
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

    private void cleanup() {
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
