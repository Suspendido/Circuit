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
                .append("uuid", this.uuid.toString())
                .append("name", this.name)
                .append("display", this.display)
                .append("purchasable", this.purchasable)
                .append("permission", this.permission)
                .append("createdAt", this.createdAt)
                .append("createdBy", this.createdBy != null ? this.createdBy.toString() : null);
    }
}
