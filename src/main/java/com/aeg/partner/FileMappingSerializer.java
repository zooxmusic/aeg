package com.aeg.partner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by bszucs on 4/6/2016.
 */
public class FileMappingSerializer implements JsonSerializer<FileMapping> {
    @Override
    public JsonElement serialize(FileMapping fileMapping, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("local", fileMapping.getLocal());
        jsonObject.addProperty("remote", fileMapping.getRemote());
        jsonObject.addProperty("pattern", fileMapping.getPattern());
        return jsonObject;
    }
}
