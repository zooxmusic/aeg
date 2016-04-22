package com.aeg.transfer;

import com.aeg.mail.MailMan;
import com.aeg.partner.FileMapping;
import com.aeg.partner.Partner;
import com.aeg.partner.PartnerHolder;
import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by bszucs on 4/10/2016.
 */
public class FtpTransferService implements TransferService {

    private Logger log = LogManager.getLogger(FtpTransferService.class.getName());

    private static final String COMPLETED_EXPRESSION = ".done";

    private FTPClient ftp = null;

    public static FtpTransferService create() {
        return new FtpTransferService();
    }

    public FtpTransferService() {
    }

    public void outbound() throws IOException, URISyntaxException {
        outbound(null);
    }

    public void outbound(String partnerName) throws IOException, URISyntaxException {
        log.info(">>>  Outbound...");
        for (Partner partner : getPartner(partnerName)) {

            Connection conn = getConnection(partner);

            for (FileMapping fileMapping : partner.getOutboundFileMappings()) {

                String localDir = fileMapping.getLocal();
                String remoteDir = fileMapping.getRemote();
                String pattern = fileMapping.getPattern();

                log.info(">>>   LOCAL DIR: " + localDir);
                log.info(">>>   REMOTE DIR: " + remoteDir);
                log.info(">>>   PATTERN: " + pattern);

                try {
                    outbound(conn, localDir, pattern, remoteDir);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MailMan.deliver();
                }
            }
        }
    }

    private void outbound(final Connection connection, final String localDir, final String filter, final String remoteDir) throws SftpException, IOException, JSchException, InterruptedException {

        try {
            log.info(">>>   reading INSIDE outbound: ");
            log.info(">>>   LOCAL DIR: " + localDir);
            log.info(">>>   REMOTE DIR: " + remoteDir);
            log.info(">>>   FILTER: " + filter);

            connect(connection);

            boolean bFilter = (null != filter || "".equalsIgnoreCase(filter.trim()));

            String tmpRemote = remoteDir;
            if (!tmpRemote.endsWith("/")) {
                tmpRemote += "/";
            }

            ftp.changeWorkingDirectory(tmpRemote);

            File dir = new File(localDir);
            if (!dir.exists()) {
                dir.mkdirs();
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

            for (File file : files) {
                FileInputStream fis = new FileInputStream(file);
                String fileName = file.getName();
                ftp.storeFile(fileName, fis);

                toRename.add(file.getAbsolutePath());
                log.info("File transfered successfully to host.");

                fis.close();
            }

            for (String fileToRename : toRename) {
                try {
                    rename(Paths.get(fileToRename));
                } catch (Exception e) {
                    e.printStackTrace();
                    String message = String.format("Failed to rename local file [%s]", fileToRename);
                    log.error(message);
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

    private List<Partner> getPartner(String name) throws IOException, URISyntaxException {
        if (null == name || "".equalsIgnoreCase(name.trim())) return PartnerHolder.getInstance().getPartnerList();
        List<Partner> partners = new ArrayList<Partner>();

        Partner partner = PartnerHolder.getInstance().find(name);
        if (null != partner) {
            partners.add(partner);
        }
        return partners;
    }

    private void rename(Path file) throws IOException {
        String newName = String.format("%s%s", file.getFileName().toString(), COMPLETED_EXPRESSION);
        Path dir = file.getParent();
        Path fn = file.getFileSystem().getPath(newName);
        Path target = (dir == null) ? fn : dir.resolve(fn);
        Files.move(file, target);
    }

    public void inbound() throws IOException, URISyntaxException {
        inbound(null);
    }

    public void inbound(String partnerName) throws IOException, URISyntaxException {
        log.info("<<< Inbound...");
        for (Partner partner : getPartner(partnerName)) {

            Connection conn = getConnection(partner);

            for (FileMapping fileMapping : partner.getInboundFileMappings()) {

                String localDir = fileMapping.getLocal();
                String remoteDir = fileMapping.getRemote();
                String pattern = fileMapping.getPattern();

                log.info("<<<  REMOTE DIR: " + remoteDir);
                log.info("<<<  LOCAL DIR: " + localDir);
                log.info("<<<  PATTERN: " + pattern);

                try {
                    inbound(conn, remoteDir, pattern, localDir);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MailMan.deliver();
                }
            }
        }
    }

    // inbound means "downloading". We always look from our perspective
    private void inbound(final Connection connection, final String remoteDir, final String filter, final String localDir) throws SftpException, IOException, JSchException {
        log.info("<<<  reading INSIDE inbound: ");
        log.info("<<<  REMOTE DIR: " + remoteDir);
        log.info("<<<  LOCAL DIR: " + localDir);
        log.info("<<<  FILTER: " + filter);

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

            FTPFileFilter ftpFilter = new FTPFileFilter() {

                @Override
                public boolean accept(FTPFile ftpFile) {

                    return (ftpFile.isFile() && ftpFile.getName().endsWith(sanitizeFilter(filter)));
                }
            };
            String tmpRemote = remoteDir;
            if (!tmpRemote.endsWith("/")) {
                tmpRemote += "/";
            }

            ftp.changeWorkingDirectory(tmpRemote);

            Vector<String> toRename = new Vector<String>();

            FTPFile[] ftpFiles = ftp.listFiles(remoteDir, ftpFilter);
            for (FTPFile ftpFile : ftpFiles) {
                try {
                    String oldName = ftpFile.getName();
                    String newName = oldName + COMPLETED_EXPRESSION;

                    String downloaded = tmpLocal + ftpFile.getName();
                    toRename.add(oldName);

                    //get output stream
                    OutputStream output = new FileOutputStream(downloaded);
                    //get the file from the remote system
                    boolean gotFile = ftp.retrieveFile(ftpFile.getName(), output);

                    if(!gotFile) {
                        log.error("Error retrieving file");
                    }
                    //ftpFile.setName(newName);
                    //delete the file
                    //ftp.deleteFile(oldName);
                    //boolean renamed = ftp.rename(oldName, newName);

                    //if(!renamed) {
                    //    log.error("Error renaming FTP file");
                   // }
                    //close output stream
                    output.flush();
                    output.close();


                    String message = String.format("Remote File: [%s] was downloaded to %s%s and then renamed to [%s]", oldName, tmpLocal, oldName, newName);
                    log.info(message);

                    //ftp.deleteFile(oldName);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }
            }

            ftp.changeWorkingDirectory(tmpRemote);

            // rename them all
            for(String downloaded : toRename) {
                try {

                    //File localFile = new File(downloaded);
                    //FileInputStream fis = new FileInputStream(localFile);
                    //String fileName = localFile.getName() + COMPLETED_EXPRESSION;
                    String fileName = downloaded + COMPLETED_EXPRESSION;
                    ftp.rename(downloaded, fileName);
                    //ftp.storeFile(fileName, fis);

                    log.info("File transfered successfully to host.");

                    //fis.close();


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

    private String sanitizeFilter(String filter) {
        if(null == filter || "".equalsIgnoreCase(filter)) return ".*";
        try {
            int indexOfDot = filter.indexOf(".");
            return filter.substring(indexOfDot);
        }catch(Exception e) {
            return ".*";
        }
    }

    private void connect(final Connection connection) throws IOException {
        log.info("Connecting to sftp...");

        //new ftp client
        ftp = new FTPClient();
        ftp.setDefaultPort(connection.getPort());
        //try to connect
        ftp.connect(connection.getHost());
        //login to server
        if(!ftp.login(connection.getUsername(), connection.getPassword())) {
            ftp.logout();
            throw new IOException("Error logging into the FTP server");
        }
        ftp.enterLocalPassiveMode();
        log.info("FTP client connected.");

    }

    private void cleanup() throws IOException {
        if (null != ftp) {
            ftp.logout();
            ftp.disconnect();
            log.info("sftp Channel exited.");
        }
    }

}
