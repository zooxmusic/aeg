package com.aeg.transfer;

import com.aeg.partner.Partner;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by bszucs on 4/20/2016.
 */
public interface TransferService {
    public void outbound(Partner partner) throws Exception;
    public void inbound(Partner partner) throws IOException, URISyntaxException;

}
