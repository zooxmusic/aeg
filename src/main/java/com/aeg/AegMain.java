package com.aeg;

import com.aeg.partner.Partner;
import com.aeg.partner.PartnerHolder;
import com.aeg.transfer.TransferService;
import com.aeg.transfer.TransferServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by bszucs on 4/11/2016.
 */
public class AegMain {

    private static String IN = "in";
    private static String OUT = "out";
    private static String BOTH = "both";

    private TransferService transferService = null;

    private static Logger log = LogManager.getFormatterLogger(AegMain.class.getName());

    // direction (both | inbound | outbound)
    // partner, a name or code or nothing. Nothing means all
    public static void main(String[] args) throws IOException, URISyntaxException {
        log.info("we are about to embark on a great journey;");

        String direction = BOTH;
        String partnerName = null;
        AegMain main = new AegMain();

        // if this logic is true then we are looking for both direction and a partner name (code)
        if(args == null && args.length == 0) {
            main.transfer(BOTH, "");
            return;
        }else if(args != null && args.length == 2) {
            String testDir = args[0];
            if(isNotValidDirection(testDir)) {
                log.error("You must pass a direction as the first parameter. As either both, in or out");
                System.exit(1);
            }

            String testPartnerName = args[1];
            direction = testDir;
            main.transfer(direction, testPartnerName);
        } else if(args != null && args.length == 1) {
            String testValue = args[0];

            if(isValidDirection(testValue)) {
                main.transfer(direction, "");
                return;
            } else {
                main.transfer(BOTH, testValue);
                return;
            }
        } else {
            log.error("You have passed too many parameters");
            System.exit(1);
        }
    }

    private static boolean isNotValidDirection(String dir) {
        return ! isValidDirection(dir);
    }

    private static boolean isValidDirection(String dir) {
        return dir.equalsIgnoreCase(IN) || dir.equalsIgnoreCase(OUT) || dir.equalsIgnoreCase(BOTH);
    }


    private void transfer(String direction, String name) throws IOException, URISyntaxException {
        log.info("USING JSON: " + PartnerHolder.getInstance().getJson());
        Partner partner = PartnerHolder.getInstance().find(name);
        transferService = TransferServiceFactory.create(partner);

        if ("in".equalsIgnoreCase(direction)) {
            transferIn(name);
        } else if ("out".equalsIgnoreCase(direction)) {
            transferOut(name);
        } else {
            transferIn(name);
            transferOut(name);
        }
    }

    private void transferIn(String name) {
        //SftpTransferService transferService = SftpTransferService.create();

        try {
            transferService.inbound(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transferOut(String name) {

        //SftpTransferService transferService = SftpTransferService.create();

        try {
            transferService.outbound(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
