package com.aeg.partner;

import com.aeg.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PartnerHolder {

    private static PartnerHolder INSTANCE = null;

    public static PartnerHolder getInstance() throws Exception {
        if (null == INSTANCE) {
            INSTANCE = new PartnerHolder();
        }
        return INSTANCE;
    }

    private String partnersFilePath = "%s/config//partners.json";
    private String json;
    //private Path partnersJson;

    private PartnerHolder() throws Exception {
        read();
    }

    private Partners partners = null;

    public Partner find(String name) throws IOException, URISyntaxException {
        for (Partner partner : partners.getPartners()) {
            if (partner.getName().toUpperCase().equalsIgnoreCase(name.toUpperCase())) return partner;
        }
        return null;
    }

    private void read() throws Exception {


        /*File file = new File(partnersFilePath);
        List<String> lines = Files.readAllLines(Paths.get(file.toURI()));
        StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            builder.append(line);
        }*/

        String aegHome = System.getenv().get("AEG_HOME");
        if(null == aegHome || "".equalsIgnoreCase(aegHome)) {
            throw new Exception("AEG_HOME is not set");
        }

        String path = String.format(partnersFilePath, aegHome);
        String tmp = FileUtil.readFile(path);

        json = sanitize(aegHome, tmp);
        partners = new Gson().fromJson(json, Partners.class);
    }

    /*public void init() throws Exception {
        URL url = PartnerHolder.class.getResource(partnersFilePath);
        partnersJson = Paths.get(url.toURI());
        read();
    }*/
    private String sanitize(String aegHome, String original) {
        return original.replaceAll("#AEG_HOME", aegHome);
    }

    public Partners getPartners() {
        return partners;
    }

    public String getJson() {
        return json;
    }

    public List<Partner> getPartnerList() {
        return getPartners().getPartners();
    }
}
