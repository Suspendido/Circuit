package com.sylluxpvp.circuit.shared.grant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.tools.circuit.Serializable;

import java.util.UUID;

@Getter @Setter @RequiredArgsConstructor
public class Grant<T extends Serializable> {

    private final UUID uuid;
    private final T data;
    private final UUID author;

    private long duration;
    private long timeCreated;
    private String reason;
    private boolean removed;
    private UUID removedBy;
    private long removedAt;
    private String removalReason;

    public boolean isActive() {
        return !isRemoved() && !isExpired();
    }

    public boolean isExpired() {
        if (duration == -1) return false;
        return System.currentTimeMillis() > this.timeCreated + this.duration;
    }

    public String getReason() {
        return this.reason == null ? "Unspecified" : this.reason;
    }

    public Document toDocument() {
        Document doc = new Document()
                .append("uuid", this.uuid.toString())
                .append("author", this.author.toString())
                .append("duration", this.duration)
                .append("timeCreated", this.timeCreated)
                .append("reason", this.reason)
                .append("removed", this.removed)
                .append("removedBy", this.removedBy != null ? this.removedBy.toString() : null)
                .append("removedAt", this.removedAt)
                .append("removalReason", this.removalReason);

        if (this.data != null) {
            doc.append("dataType", this.data.getType());
            doc.append("dataID", this.data.getID());
            if (!data.getType().equalsIgnoreCase("rank")) {
                doc.append("dataContent", this.data.toDocument());
            }
        }

        return doc;
    }
}
