package com.aeg.transfer;

import com.aeg.partner.Partner;

/**
 * Created by bszucs on 4/20/2016.
 */
public class TransferServiceFactory {

    public static TransferService create(Partner partner) {
        if(partner.getFtp()) {
            return new FtpTransferService();
        }
        return new SftpTransferService();
    }

}
