package com.aeg.transfer.saved;


import com.aeg.transfer.TransferService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by bszucs on 4/10/2016.
 */
public class TransferServiceTest {
    public static void main(String[] args) {

        TransferService transferService = TransferService.create();

        try {
            transferService.inbound("CR");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        TransferService transferService2 = TransferService.create();
        try {
            transferService2.outbound("CR");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}
