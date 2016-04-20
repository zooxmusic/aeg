package com.aeg.partner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by bszucs on 4/6/2016.
 */
public class PartnerSerializer implements JsonSerializer<Partner> {
    @Override
    public JsonElement serialize(Partner partner, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", partner.getName());
        jsonObject.addProperty("ftp", partner.getFtp());
        jsonObject.addProperty("host", partner.getHost());
        jsonObject.addProperty("port", partner.getPort());
        jsonObject.addProperty("username", partner.getUsername());
        jsonObject.addProperty("password", partner.getPassword());

        final JsonElement jsonInboundFileMappings = jsonSerializationContext.serialize(partner.getInboundFileMappings());
        final JsonElement jsonOutboundFileMappings = jsonSerializationContext.serialize(partner.getInboundFileMappings());
        jsonObject.add("inboundFileMappings", jsonInboundFileMappings);
        jsonObject.add("outboundFileMappings", jsonOutboundFileMappings);

        return jsonObject;
    }
}
