package com.aeg;

import com.aeg.mail.MailMan;
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

    private static Logger log = LogManager.getFormatterLogger(AegMain.class.getName());

    /**
     * We can pass 0 to 2 parameters. With no parameters we assume all partners in both directions
     * if we have just a direction then we have all partners in the direction passed
     * if we only have a partner then we have both directions with that partner
     * if we have both then we do the explicit direction with that partner
     * @param args direction (both | inbound | outbound) partner, a name or code or nothing. Nothing means all
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
        log.info("we are about to embark on a great journey;");
        AegMain main = new AegMain();
        try {
            main.process(args);
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
        System.exit(0);
    }

    private void process(String...args) throws Exception {

        log.info("Begin processing FTP/SFTP");

        if(hasTestMail(args)) {
            sendTestMail();
            return;
        }

        // if args is null or empty then we will process all partners in both directions
        if (args == null || args.length == 0) {

            log.info(String.format("We received no arguments"));
            processAllPartnersInBothDirections();


        // we have at least 1 argument
        } else if (args != null && args.length > 0) {

            // we only have 1 argument
            if(args.length == 1) {

                String arg1 = args[0];
                // we will process both directions for the given partner
                if(isValidPartner(arg1)) {
                    log.info(String.format("We received 1 arguments as a valid partner: %s", arg1));

                    processPartnerInBothDirections(arg1);

                // we will process all partners for given direction
                } else if(isValidDirection(arg1)) {
                    log.info(String.format("We received 1 arguments as a valid direction: %s", arg1));

                    processAllPartnersForGivenDirection(arg1);

                } else {
                    log.info(String.format("We received 1 arguments but is not a valid partner or direction: %s", arg1));

                    throw new Exception(String.format("You must pass a valid direction or partner, received Argument1: %s", arg1));
                }

            // we have both arguments
            } else if(args.length == 2) {

                String arg1 = args[0];
                String arg2 = args[1];

                if(hasNoValidDirection(arg1, arg2) && hasNoValidPartner(arg1, arg2)) {
                    log.info(String.format("We received 2 arguments but they are not valid as a partner or a direction. Argument1 %s  Argument2: %s", arg1, arg2));
                    throw new Exception(String.format("You must pass a valid direction and partner, received Argument1: %s  and Argument2: %s", arg1, arg2));
                }

                // if we can determine if both are valid then we can continue
                if(hasValidDirection(arg1, arg2) && hasValidPartner(arg1, arg2)) {
                    // this one will be if arguments are passed as expected (direction partner
                    if(isValidDirection(arg1) && isValidPartner(arg2)) {
                        log.info(String.format("We received 2 arguments, Partner: %s and Direction: %s", arg2, arg1));

                        processPartnerInGivenDirection(arg1, arg2);

                    // arguments are passed reversed. We could have stopped processing but I will handle it
                    } else if(isValidPartner(arg1) && isValidDirection(arg2)) {
                        log.info(String.format("We received 2 arguments, Partner: %s and Direction: %s", arg1, arg2));

                        processPartnerInGivenDirection(arg2, arg1);
                    } else {
                        //we really should never be here
                        log.info(String.format("We received 2 arguments but they are not valid as a partner or a direction. Argument1 %s  Argument2: %s", arg1, arg2));
                        throw new Exception(String.format("You must pass a valid direction and partner, received Argument1: %s  and Argument2: %s", arg1, arg2));
                    }
                } else {
                    log.info(String.format("We received 2 arguments but they are not valid as a partner or a direction. Argument1 %s  Argument2: %s", arg1, arg2));
                    throw new Exception(String.format("You must pass a valid direction and partner, received Argument1: %s  and Argument2: %s", arg1, arg2));
                }
            }

            // we should never hit this logic but its a fail safe
        } else {
            log.info(String.format("Somethin bad about to happen"));
            throw new Exception("Something really bad happened");
        }
    }

    private boolean hasTestMail(String...args) {
        for(String arg : args) {
            if(arg.toLowerCase().contains("mail")) {
                return true;
            }
        }
        return false;
    }

    private void sendTestMail() {
        MailMan.deliverTest();
    }

    private void processAllPartnersInBothDirections() throws Exception {
        for(Partner partner : PartnerHolder.getInstance().getPartnerList()) {
            transferIn(partner);
            transferOut(partner);
        }
    }

    private void processAllPartnersForGivenDirection(String direction) throws Exception {

        for(Partner partner : PartnerHolder.getInstance().getPartnerList()) {
            if(IN.equalsIgnoreCase(direction)) {
                transferIn(partner);
            } else {
                transferOut(partner);
            }
        }
    }



    private boolean hasNoValidDirection(String...args) {
        return ! hasValidDirection(args);
    }
    private boolean hasValidDirection(String...args) {
        for(String arg : args) {
            if(isValidDirection(arg)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNoValidPartner(String...args) throws Exception {
        return ! hasValidPartner(args);
    }
    private boolean hasValidPartner(String...args) throws Exception {
        for(String arg : args) {
            if(isValidPartner(arg)) {
                return true;
            }
        }
        return false;
    }
    private void processPartnerInBothDirections(String partnerName) throws Exception {
        Partner partner = getPartner(partnerName);
        transferIn(partner);
        transferOut(partner);
    }

    private void processPartnerInGivenDirection(String direction, String partnerName) throws Exception {
        Partner partner = getPartner(partnerName);
        if(IN.equalsIgnoreCase(direction)) {
            transferIn(partner);
        } else {
            transferOut(partner);
        }
    }

    private boolean isNotValidDirection(String dir) {
        return ! isValidDirection(dir);
    }

    private Partner getPartner(String name) throws Exception {
        return PartnerHolder.getInstance().find(name);
    }
    private boolean isValidDirection(String dir) {
        String temp = dir.toUpperCase();
        return temp.equalsIgnoreCase(IN) || temp.equalsIgnoreCase(OUT) || temp.equalsIgnoreCase(BOTH);
    }

    private boolean isNotValidPartner(String partnerName) throws Exception {
        return ! isValidPartner(partnerName);
    }
    private boolean isValidPartner(String partnerName) throws Exception {
        return PartnerHolder.getInstance().find(partnerName) != null;
    }

    private void transferIn(Partner partner) {
        TransferService transferService = TransferServiceFactory.create(partner);

        try {
            transferService.inbound(partner);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void transferOut(Partner partner) {

        TransferService transferService = TransferServiceFactory.create(partner);

        try {
            transferService.outbound(partner);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
