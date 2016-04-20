package com.aeg.partner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bszucs on 4/6/2016.
 */
public class Partners {
    private List<Partner> partners = new ArrayList<Partner>();

    public void setPartners(List<Partner> partners) {
        this.partners = partners;
    }

    public void addPartner(Partner partner) {
        this.partners.add(partner);
    }

    public List<Partner> getPartners() {
        return partners;
    }
}
