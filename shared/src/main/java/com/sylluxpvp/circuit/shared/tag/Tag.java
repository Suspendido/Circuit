package com.sylluxpvp.circuit.shared.tag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class Tag {

    private final UUID uuid;
    private final String name;
    @Setter private String display = "";
    @Setter private boolean purchasable = false;
    @Setter private String permission = "";
    @Setter private long createdAt = System.currentTimeMillis();
    @Setter private UUID createdBy;

    public Document toDocument() {
        return new Document()
                .append("uuid", uuid.toString())
                .append("name", name)
                .append("display", display)
                .append("purchasable", purchasable)
                .append("permission", permission)
                .append("createdAt", createdAt)
                .append("createdBy", createdBy != null ? createdBy.toString() : null);
    }
}
