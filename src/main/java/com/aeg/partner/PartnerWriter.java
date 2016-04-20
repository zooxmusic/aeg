package com.aeg.partner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class PartnerWriter {

    /*public static void main(String[] args) {
        writeDefaultData();
    }*/

    public static void writeDefaultData() {
        Partners partners = new Partners();
        Partner cr = new Partner();
        cr.setName("CR");
        cr.setHost("vault.clearesult.com");
        cr.setPort(22);
        cr.setUsername("bszucs");
        cr.setPassword("109F0r3$t");
        cr.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/CR/XML_Applications", "/HVAC - Files For Testing/");
        cr.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/CR/XML_Applications", "/HPwES - Files For Testing/");
        cr.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/CR/XML_Applications", "/RNC - Files For Testing/");
        cr.addOutboundMapping("C:/AEG/IMSTransferFiles/Outbound/CR/CHECK_STATUS/HPWES", "/HPwES - Files For Testing");
        cr.addOutboundMapping("C:/AEG/IMSTransferFiles/Outbound/CR/CHECK_STATUS/HVAC", "/HVAC - Files For Testing");
        cr.addOutboundMapping("C:/AEG/IMSTransferFiles/Outbound/CR/CHECK_STATUS/RNC", "/RNC - Files For Testing");

        cr.addOutboundMapping("C:/AEG/IMSTransferFiles/Outbound/CR/QA_STATUS/HPWES", "/RNC - Files For Testing");
        cr.addOutboundMapping("C:/AEG/IMSTransferFiles/Outbound/CR/QA_STATUS/HVAC", "/RNC - Files For Testing");
        cr.addOutboundMapping("C:/AEG/IMSTransferFiles/Outbound/CR/QA_STATUS/RNC", "/RNC - Files For Testing");

        partners.addPartner(cr);

        Partner crh = new Partner();
        crh.setName("CRH");
        crh.setHost("vault.clearesult.com");
        crh.setPort(22);
        crh.setUsername("bszucs");
        crh.setPassword("109F0r3$t");
        crh.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/CRH/XML_Applications", "/HVAC - Files For Testing/");
        crh.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/CRH/New", "/HPwES - Files For Testing/");
        crh.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/CRH/XML_Applications", "/RNC - Files For Testing/");
        partners.addPartner(crh);

        Partner icf = new Partner();
        icf.setName("ICF");
        icf.setHost("transferInbound.icfwebservices.com");
        icf.setPort(22);
        icf.setUsername("NJCEP_test");
        icf.setPassword("5YF08pcm");
        icf.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/ICF/XML_CheckStatus", "/FromICF/CheckConfirmations/");
        icf.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/ICF/XML_CheckStatus", "/FromICF/CheckStopClearVoids/");
        partners.addPartner(icf);

        Partner aeg = new Partner();
        aeg.setName("AEG");
        aeg.setHost("sftp.ameresco.com");
        aeg.setPort(22);
        aeg.setUsername("sftp_a037_g30");
        aeg.setPassword("$mHs#4&U");

        aeg.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/AEG/XML_Applications", "/toIMS_UAT/SRP/");
        aeg.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/ICF/XML_Applications", "/toIMS_UAT/EEP/");
        aeg.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/ICF/XML_Applications", "/toIMS_UAT/REIP/");
        aeg.addInboundMapping("C:/AEG/IMSTransferFiles/Inbound/ICF/XML_Applications", "/toIMS_UAT/CHP/");

        cr.addOutboundMapping("C:/AEG/IMSTransferFiles/Outbound/AEG/VISION_EEP", "/fromIMS_UAT/Vision/EEP/");
        cr.addOutboundMapping("C:/AEG/IMSTransferFiles/Outbound/AEG/VISION_NONSOLAR", "/fromIMS_UAT/Vision/NONSOLOAR/");
        cr.addOutboundMapping("C:/AEG/IMSTransferFiles/Outbound/AEG/VISION_SRP", "/fromIMS_UAT/Vision/SRP/");

        partners.addPartner(aeg);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Partner.class, new PartnerSerializer());
        gsonBuilder.registerTypeAdapter(Partner.class, new PartnerDeserializer());
        gsonBuilder.registerTypeAdapter(FileMapping.class, new FileMappingSerializer());
        gsonBuilder.registerTypeAdapter(FileMapping.class, new FileMappingDeserializer());
        Gson gson = gsonBuilder.setPrettyPrinting().create();
        String json = gson.toJson(partners);
        System.out.println(json);
    }

}
