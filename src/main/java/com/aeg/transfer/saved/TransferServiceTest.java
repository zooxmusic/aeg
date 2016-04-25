package com.aeg.transfer.saved;


import com.aeg.partner.Partner;
import com.aeg.partner.PartnerHolder;
import com.aeg.transfer.SftpTransferService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by bszucs on 4/10/2016.
 */
public class TransferServiceTest {
    public static void main(String[] args) {

        SftpTransferService transferService = SftpTransferService.create();

        try {
            transferService.inbound(PartnerHolder.getInstance().find("cr"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        SftpTransferService transferService2 = SftpTransferService.create();
        try {
            transferService2.outbound(PartnerHolder.getInstance().find("cr"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
