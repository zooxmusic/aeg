package com.aeg.transfer;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by bszucs on 4/20/2016.
 */
public interface TransferService {
    public void outbound() throws IOException, URISyntaxException;
    public void outbound(String partnerName) throws IOException, URISyntaxException;

    public void inbound() throws IOException, URISyntaxException;
    public void inbound(String partnerName) throws IOException, URISyntaxException;

}
