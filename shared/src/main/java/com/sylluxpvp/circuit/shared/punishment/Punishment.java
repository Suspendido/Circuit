package com.sylluxpvp.circuit.shared.punishment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.tools.circuit.Serializable;

import java.util.UUID;

@Getter @Setter
@RequiredArgsConstructor
public class Punishment implements Serializable {

    private final UUID UUID;
    private final PunishmentType punishmentType;

    @Override
    public String getID() {
        return this.UUID.toString();
    }

    public String getType() {
        return "punishment";
    }

    @Override
    public String getExpiryMessage() {
        return "&aYou're no longer " + punishmentType.getAction() + "!";
    }

    @Override
    public String getRemovalMessage() {
        return "";
    }

    @Override
    public Document toDocument() {
        return new Document().append("uuid", this.UUID.toString()).append("type", punishmentType.name());
    }
}
