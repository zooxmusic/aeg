package com.aeg;

import com.aeg.partner.PartnerHolder;
import com.aeg.transfer.TransferService;
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

    private static Logger log = LogManager.getFormatterLogger(AegMain.class.getName());

    // direction (both | inbound | outbound)
    // partner, a name or code or nothing. Nothing means all
    public static void main(String[] args) throws IOException, URISyntaxException {
        log.info("we ae about to embark on a great journey;");

        String direction = BOTH;
        if (null == args || args.length < 1) {
            log.error("You must pass a direction as the first parameter. As either both, in or out");
            System.exit(1);
        }

        String dirction = args[0];
        String name = null;
        if (args.length < 2) {
            log.info("calling transfer process for all partners");
        } else {
            name = args[1];
            log.info("calling transfer for explicit partners, I will log in the future");
        }

        AegMain main = new AegMain();
        main.transfer(dirction, name);


    }

    private void transfer(String direction, String name) throws IOException, URISyntaxException {
        log.info("USING JSON: " + PartnerHolder.getInstance().getJson());
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

        TransferService transferService = TransferService.create();

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

        TransferService transferService = TransferService.create();

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
