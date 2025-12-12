package com.sylluxpvp.circuit.shared.tools.gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.GrantService;

import java.io.IOException;

public class GrantAdapter extends TypeAdapter<Grant<?>> {

    @Override
    public void write(JsonWriter out, Grant<?> grant) throws IOException {
        if (grant == null) {
            out.nullValue();
            return;
        }
        Document doc = grant.toDocument();
        out.jsonValue(doc.toJson());
    }

    @Override
    public Grant<?> read(JsonReader in) {
        JsonObject obj = JsonParser.parseReader(in).getAsJsonObject();
        Document doc = Document.parse(obj.toString());
        return ServiceContainer.getService(GrantService.class).fromDocument(doc);
    }
}