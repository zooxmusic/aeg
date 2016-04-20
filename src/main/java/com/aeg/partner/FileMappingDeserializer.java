package com.aeg.partner;

import com.google.gson.*;

import java.lang.reflect.Type;


public class FileMappingDeserializer implements JsonDeserializer<FileMapping> {
    @Override
    public FileMapping deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        FileMapping fileMapping = new FileMapping();

        fileMapping.setLocal(jsonObject.get("local").getAsString());
        fileMapping.setRemote(jsonObject.get("remote").getAsString());
        fileMapping.setPattern(jsonObject.get("pattern").getAsString());

        return fileMapping;
    }
}
