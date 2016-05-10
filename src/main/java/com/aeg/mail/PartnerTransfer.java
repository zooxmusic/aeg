package com.aeg.mail;

import com.aeg.partner.Partner;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by bszucs on 5/4/2016.
 */
public class PartnerTransfer {

    private Partner partner;

    private DateTime startedAt;
    private DateTime endedAt;
    private Duration processingTime;

    private Set<TransferFile> files = new HashSet<TransferFile>();

    public static PartnerTransfer create(Partner partner) {
        return new PartnerTransfer(partner);
    }
    private PartnerTransfer(Partner partner) {
        this.partner = partner;
        this.startedAt = DateTime.now();
    }
}
