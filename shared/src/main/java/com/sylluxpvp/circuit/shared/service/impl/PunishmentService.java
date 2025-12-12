package com.sylluxpvp.circuit.shared.service.impl;

import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.punishment.Punishment;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;
import com.sylluxpvp.circuit.shared.service.NoActionService;

import java.util.UUID;

public class PunishmentService extends NoActionService {

    @Override
    public @NonNull String getIdentifier() {
        return "punishment";
    }

    public Grant<Punishment> create(UUID author, PunishmentType type, String reason, long duration) {
        Grant<Punishment> punishment = new Grant<>(UUID.randomUUID(), new Punishment(UUID.randomUUID(), type), author);
        punishment.setDuration(duration);
        punishment.setReason(reason);
        punishment.setTimeCreated(System.currentTimeMillis());
        return punishment;
    }

    public Grant<Punishment> create(UUID author, PunishmentType type, String reason) {
        Grant<Punishment> punishment = new Grant<>(UUID.randomUUID(), new Punishment(UUID.randomUUID(), type), author);
        punishment.setDuration(-1);
        punishment.setReason(reason);
        punishment.setTimeCreated(System.currentTimeMillis());
        return punishment;
    }

    public void removePunishment(UUID author, Profile profile, Grant<Punishment> punishment, String reason) {
        Validate.notNull(author, "Author cannot be null");
        Validate.notNull(profile, "Profile cannot be null");
        Validate.notNull(punishment, "Punishment cannot be null");
        punishment.setRemoved(true);
        punishment.setRemovalReason(reason);
        punishment.setRemovedBy(author);
    }

    @NonNull
    public Punishment fromDocument(Document document) {
        Validate.notNull(document, "Document cannot be null");
        return new Punishment(UUID.fromString(document.getString("uuid")), PunishmentType.valueOf(document.getString("type")));
    }
}
