package com.aeg.transfer;

import com.aeg.partner.Partner;
import com.aeg.pgp.PGPFileProcessor;
import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

/**
 * Created by bszucs on 4/10/2016.
 */
public class FtpTransferService extends AbstractTransferService {

    private Logger log = LogManager.getLogger(FtpTransferService.class.getName());

    private FTPClient ftp = null;

    public static FtpTransferService create() {
        return new FtpTransferService();
    }


    protected void outbound(Vector<File> filesToTransfer, File localDir, File processedDir, String remoteDir, Partner partner) throws Exception {

        Vector<File> filesToMove = new Vector<File>();

        ftp.changeWorkingDirectory(remoteDir);


        for (File file : filesToTransfer) {

            FileInputStream fis = new FileInputStream(file);

            // copy to their server
            ftp.storeFile(file.getName(), fis);

            fis.close();

            log.info(String.format("File %s transferred successfully to host.", file.getName()));

            filesToMove.add(file);
        }

        // move them all to processed
        moveFilesToProcessed(localDir, filesToMove);

        // maybe delete the originals

    }
    protected Vector<String> inbound(final String remoteDir, final String filter, final File localDir, final Partner partner) throws Exception {

        String localFilePath = "";
        Vector<String> processedLocalFiles = new Vector<String>();

        try {

            ftp.changeWorkingDirectory(remoteDir);

            FTPFile[] files = ftp.listFiles(remoteDir, getFilter(filter));

            for (FTPFile ftpFile : files) {

                try {
                    //get output stream
                    localFilePath = String.format("%s/%s", localDir.getAbsolutePath(), ftpFile.getName());
                    OutputStream output = new FileOutputStream(localFilePath);
                    boolean gotFile = ftp.retrieveFile(ftpFile.getName(), output);

                    if (!gotFile) {
                        log.error("Error retrieving file");
                    }
                    output.flush();
                    output.close();
                }catch(Exception e) {
                    log.error(e.getMessage(), e);
                }

                ftp.changeWorkingDirectory(remoteDir);

                try {

                    String from = String.format("%s/%s", remoteDir, ftpFile.getName());
                    String to = String.format("%s/done/%s", remoteDir, ftpFile.getName());
                    ftp.rename(from, to);

                    String message = String.format("Remote File: [%s] was downloaded to %s and then moved to to [%s]", remoteDir, localFilePath, to);
                    log.info(message);


                }catch(Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        } catch (Exception e) {

            throw e;
        } finally {
            cleanup();
        }
        return processedLocalFiles;
    }

    private FTPFileFilter getFilter(final String filter) {
        return new FTPFileFilter() {

            @Override
            public boolean accept(FTPFile ftpFile) {

                return (ftpFile.isFile() && ftpFile.getName().endsWith(sanitizeFilter(filter)));
            }
        };
    }

    protected void connect(final Connection connection) throws Exception {
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

    protected void cleanup() throws Exception {
        if (null != ftp) {
            ftp.logout();
            ftp.disconnect();
            log.info("sftp Channel exited.");
        }
    }

}
