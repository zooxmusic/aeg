package com.aeg.partner;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by bszucs on 4/6/2016.
 */
public class PartnerDeserializer implements JsonDeserializer<Partner> {
    @Override
    public Partner deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        Partner partner = new Partner();
        partner.setName(jsonObject.get("name").getAsString());
        partner.setHost(jsonObject.get("host").getAsString());
        partner.setPort(jsonObject.get("port").getAsInt());
        partner.setUsername(jsonObject.get("username").getAsString());
        partner.setPassword(jsonObject.get("password").getAsString());

        Collection<FileMapping> inboundFileMappings = jsonDeserializationContext.deserialize(jsonObject.get("inboundFileMappings"), new TypeToken<Collection<FileMapping>>() {
        }.getType());
        Collection<FileMapping> outboundFileMappings = jsonDeserializationContext.deserialize(jsonObject.get("outboundFileMappings"), new TypeToken<Collection<FileMapping>>() {
        }.getType());
        partner.setInboundFileMappings(inboundFileMappings);
        partner.setOutboundFileMappings(outboundFileMappings);

        return partner;
    }
}
