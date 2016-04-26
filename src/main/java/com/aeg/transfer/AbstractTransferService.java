package com.aeg.transfer;

import com.aeg.mail.MailMan;
import com.aeg.partner.FileMapping;
import com.aeg.partner.Partner;
import com.aeg.pgp.PGPFileProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

/**
 * Created by bszucs on 4/25/2016.
 */
public abstract class AbstractTransferService implements TransferService {

    private Logger log = LogManager.getLogger(SftpTransferService.class.getName());

    private static String IMS_PRIVATE_KEY = "%s/config/ims-priv.asc";
    private static final String IMS_PRIVKEY_PASSWORD = "AEG2016!";

    protected static final String COMPLETED_EXPRESSION = "done";
    protected static final String ORIGINAL_DIRECTORY = "orig";
    protected static final String PROCESSED_DIRECTORY = "done";

    public AbstractTransferService() {
        String aegHome = System.getenv().get("AEG_HOME");
        if(null == aegHome || "".equalsIgnoreCase(aegHome)) {
            log.error("Error finding AEG_HOME");
            System.exit(1);
        }

        IMS_PRIVATE_KEY = String.format(IMS_PRIVATE_KEY, aegHome);
    }
    public void inbound(final Partner partner) throws IOException, URISyntaxException {

        log.info("<<< Inbound...");

        Connection conn = getConnection(partner);

        for (FileMapping fileMapping : partner.getInboundFileMappings()) {

            String localDir = fileMapping.getLocal();
            String remoteDir = fileMapping.getRemote();
            String filter = fileMapping.getPattern();

            log.info(String.format("<<<  REMOTE DIR: %s", remoteDir));
            log.info(String.format("<<<  LOCAL DIR: %s", localDir));
            log.info(String.format("<<<  FILTER: %s", filter));

            try {
                connect(conn);

                localDir = sanitizeDirectory(localDir);
                File localDirectory = findOrCreateDirectory(localDir);
                Vector<String> processedLocalFiles = inbound(remoteDir, filter, localDirectory, partner);

                if(partner.getEncrypted() && null != processedLocalFiles && processedLocalFiles.size() > 0) {
                    decryptAndSaveOriginals(partner, localDirectory, processedLocalFiles);
                }


            } catch (Exception e) {
                log.error(e.getMessage(), e);
                MailMan.deliver();
            }
        }

    }



    public void outbound(final Partner partner) throws Exception {

        log.info(">>>  Outbound...");

        Connection conn = getConnection(partner);

        for (FileMapping fileMapping : partner.getOutboundFileMappings()) {

            String localDir = fileMapping.getLocal();
            String remoteDir = fileMapping.getRemote();
            String filter = fileMapping.getPattern();

            log.info(String.format(">>>   LOCAL DIR: %s", localDir));
            log.info(String.format(">>>   REMOTE DIR: %s", remoteDir));
            log.info(String.format(">>>   FILTER: %s", filter));

            try {

                connect(conn);

                localDir = sanitizeDirectory(localDir);
                File localDirectory = findOrCreateDirectory(localDir);
                File processedDirectory = findOrCreateProcessedDir(localDir);
                File[] files = listLocalFiles(localDir, filter);
                Vector<String> filesToTransfer = new Vector<String>();
                Vector<File> filePointers = new Vector<File>();
                if(partner.getEncrypted()) {
                    filesToTransfer = encryptAndSaveOriginals(partner, localDirectory, files);
                } else {
                    filePointers = translateToFile(files);
                }

                outbound(filePointers, localDirectory, processedDirectory, remoteDir, partner);

            } catch (Exception e) {
                throw e;
            } finally {
                cleanup();
            }
        }
    }
    protected Vector<String> decryptAndSaveOriginals(final Partner partner, final File sourceDir, final Vector<String> processedLocalFiles) throws IOException {
        Vector<String> decryptedFiles = decryptLocalFiles(partner, processedLocalFiles);
        try {
            Thread.sleep(2000);
        }catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        saveOriginalFiles(sourceDir, processedLocalFiles);
        return decryptedFiles;
    }


    protected Vector<String> decryptLocalFiles(final Partner partner, final Vector<String> processedLocalFiles) {
        if(null == processedLocalFiles || processedLocalFiles.size() < 1) {
            return processedLocalFiles;
        }
        if(!partner.getEncrypted()) {
            return processedLocalFiles;
        }
        Vector<String> decryptedFiles = new Vector<String>();
        for(String fileName : processedLocalFiles) {
            try {
                File file = new File(fileName);
                PGPFileProcessor fileProcessor = new PGPFileProcessor();
                fileProcessor.setAsciiArmored(false);
                fileProcessor.setKeyFile(IMS_PRIVATE_KEY);
                fileProcessor.setPassphrase(IMS_PRIVKEY_PASSWORD);
                String encryptedFile = String.format("%s", file.getAbsolutePath());
                fileProcessor.setInputFile(encryptedFile);
                String decryptedFile = encryptedFile.substring(0, encryptedFile.lastIndexOf("."));
                fileProcessor.setOutputFile(decryptedFile);
                decryptedFiles.add(decryptedFile);
                fileProcessor.decrypt();
            }catch(Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return decryptedFiles;
    }

    protected Vector<String> encryptAndSaveOriginals(final Partner partner, final File sourceDir, final File[] files) throws IOException {
        Vector<String> encryptedFiles = encryptLocalFiles(partner, files);
        saveOriginalFiles(sourceDir, files);
        return encryptedFiles;
    }


    protected Vector<String> encryptLocalFiles(final Partner partner, final File[] files) {
        if(null == files || files.length < 1) {
            return translate(files);
        }
        if(!partner.getEncrypted()) {
            return translate(files);
        }
        Vector<String> filesToTransfer = new Vector<String>();
        for(File file : files) {
            try {
                PGPFileProcessor fileProcessor = new PGPFileProcessor();
                fileProcessor.setAsciiArmored(true);
                fileProcessor.setPassphrase(partner.getKeyPassword());
                fileProcessor.setInputFile(file.getAbsolutePath());
                fileProcessor.setKeyFile(SftpTransferService.class.getResource(partner.getKeyFile()).getFile());
                String encryptedFile = String.format("%s.gpg", file.getAbsolutePath());
                fileProcessor.setOutputFile(encryptedFile);
                fileProcessor.encrypt();
                filesToTransfer.add(encryptedFile);
            }catch(Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return filesToTransfer;
    }

    protected void saveOriginalFiles(File sourceDir, File[] files) throws IOException {
        Path sourcePath = Paths.get(sourceDir.getAbsolutePath());
        for(File file : files) {
            Path targetPath = Paths.get(String.format("%s/%s", getOriginalDirectory(sourceDir.getAbsolutePath()), file.getName()));
            Files.move(sourcePath, targetPath);
        }
    }

    protected void saveOriginalFiles(File sourceDir, Vector<String> files) throws IOException {
        Path sourcePath = Paths.get(sourceDir.getAbsolutePath());
        for(String fileName : files) {
            File file = new File(fileName);
            File targetDir = new File(String.format("%s/%s", getOriginalDirectory(sourceDir.getAbsolutePath()), file.getName()));
            //Path targetPath = Paths.get(String.format("%s/%s", getOriginalDirectory(sourceDir.getAbsolutePath()), file.getName()));
            file.renameTo(targetDir);
            //Files.move(sourcePath, targetPath);
        }
    }
    private Vector<String> translate(File[] files) {
        Vector<String> translated = new Vector<String>();
        for(File file : files) {
            translated.add(file.getAbsolutePath());
        }
        return translated;
    }

    private Vector<File> translateToFile(File[] files) {
        Vector<File> translated = new Vector<File>();
        for(File file : files) {
            translated.add(file);
        }
        return translated;
    }
    protected Connection getConnection(Partner partner) {
        Connection connection = new Connection();
        connection.setHost(partner.getHost());
        connection.setPort(partner.getPort());
        connection.setUsername(partner.getUsername());
        connection.setPassword(partner.getPassword());
        return connection;
    }


    protected void moveToProcessed(File localDir, Vector<String> filesToMove) throws IOException {

        for(String fileName : filesToMove) {
            File file = new File(fileName);
            String sourceDir = String.format("%s", localDir.getAbsolutePath());
            Path sourcePath = Paths.get(sourceDir);
            String targetDir = getProcessedDirectory(sourceDir);
            String targetFilePath = String.format("%s/%s", targetDir, file.getName());
            Path targetPath = Paths.get(targetFilePath);
            Files.move(sourcePath, targetPath);
        }
    }

    protected void moveFilesToProcessed(File localDir, Vector<File> filesToMove) throws IOException {

        for(File file : filesToMove) {
            String sourceDir = String.format("%s", localDir.getAbsolutePath());
            Path sourcePath = Paths.get(sourceDir);
            String targetDir = getProcessedDirectory(sourceDir);
            String targetFilePath = String.format("%s/%s", targetDir, file.getName());
            Path targetPath = Paths.get(targetFilePath);
            Files.move(sourcePath, targetPath);
        }
    }


    protected String sanitizeDirectory(String directory) {

        String tmpDir = directory;
        if (!tmpDir.endsWith("/")) {
            tmpDir += "/";
        }
        return tmpDir;
    }


    protected String sanitizeFilter(String filter) {
        if(null == filter || "".equalsIgnoreCase(filter)) return ".*";
        try {
            int indexOfDot = filter.indexOf(".");
            return filter.substring(indexOfDot);
        }catch(Exception e) {
            return ".*";
        }
    }

    protected File[] listLocalFiles(final String directory, final String filter) throws FileNotFoundException {

        File dir = new File(directory);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(filter)) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (null == files) {
            String message = String.format("Local directory [%s] was not found", directory);
            log.error(message);
            throw new FileNotFoundException(message);
        }

        return files;
    }

    private File findOrCreateDirectory(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private File findOrCreateProcessedDir(String localDir) {
        String processedDir = getProcessedDirectory(localDir);
        File processedDirectory = new File(processedDir);
        if(!processedDirectory.exists()) {
            processedDirectory.mkdirs();
        }
        return processedDirectory;
    }

    protected String getProcessedDirectory(String sourceDirectory) {
        String processedDir =  String.format("%s/%s", sourceDirectory, PROCESSED_DIRECTORY);
        return processedDir;
    }

    protected String getOriginalDirectory(String sourceDirectory) {
        String processedDir =  String.format("%s/%s", sourceDirectory, ORIGINAL_DIRECTORY);
        return processedDir;
    }

    protected abstract void connect(Connection connection) throws Exception;
    protected abstract void cleanup() throws Exception;
    protected abstract void outbound(Vector<File> filesToTransfer, File localDir, File processedDir, String remoteDir, Partner partner) throws Exception;
    protected abstract Vector<String> inbound(String remoteDir, String filter, File localDir, Partner partner) throws Exception;
}
