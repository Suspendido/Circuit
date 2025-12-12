package com.sylluxpvp.circuit.shared.service.impl;

import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.CircuitConstants;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.tools.circuit.Serializable;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.NoActionService;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;

import java.util.UUID;

public class GrantService extends NoActionService {

    @Override @NonNull
    public String getIdentifier() {
        return "grant";
    }

    public Grant<Rank> createGrant(Rank rank, UUID author, long duration, String reason) {
        Validate.notNull(rank, "Rank cannot be null");
        if (author == null) author = CircuitConstants.getConsoleUUID();
        if (reason == null) reason = "Unspecified";
        Grant<Rank> grant = new Grant<>(UUID.randomUUID(), rank, author);
        grant.setDuration(duration);
        grant.setReason(reason);
        grant.setTimeCreated(System.currentTimeMillis());
        return grant;
    }

    public Grant<Rank> createGrant(Rank rank, UUID author, String reason) {
        Validate.notNull(rank, "Rank cannot be null");
        if (author == null) author = CircuitConstants.getConsoleUUID();
        if (reason == null) reason = "Unspecified";
        Grant<Rank> grant = new Grant<>(UUID.randomUUID(), rank, author);
        grant.setDuration(-1);
        grant.setReason(reason);
        grant.setTimeCreated(System.currentTimeMillis());
        return grant;
    }

    public Grant<Rank> getDefaultGrant() {
        Grant<Rank> grant = new Grant<>(UUID.randomUUID(), ServiceContainer.getService(RankService.class).getDefaultRank(), CircuitConstants.getConsoleUUID());
        grant.setDuration(-1);
        grant.setReason("Default Grant");
        grant.setTimeCreated(System.currentTimeMillis());
        return grant;
    }

    public Grant<? extends Serializable> fromDocument(Document doc) {
        Validate.notNull(doc, "Document cannot be null");
        UUID uuid = UUID.fromString(doc.getString("uuid"));
        UUID author = UUID.fromString(doc.getString("author"));

        Serializable data = null;
        String dataType = doc.getString("dataType");
        if (dataType != null) {
            if (dataType.equalsIgnoreCase("rank")) {
                UUID rankID = null;
                try {
                    rankID = UUID.fromString(doc.getString("dataID"));
                } catch (Exception ignored) {}
                RankService rankService = ServiceContainer.getService(RankService.class);
                data = (rankID != null && rankService.getRank(rankID) != null) ? rankService.getRank(rankID) : rankService.getDefaultRank();
            } else {
                data = Serializable.fromDocument(dataType, (Document) doc.get("dataContent"));
            }
        }

        Grant<Serializable> grant = new Grant<>(uuid, data, author);
        grant.setDuration(doc.getLong("duration"));
        grant.setTimeCreated(doc.getLong("timeCreated"));
        grant.setReason(doc.getString("reason"));
        grant.setRemoved(doc.getBoolean("removed"));
        grant.setRemovedAt(doc.getLong("removedAt"));
        grant.setRemovalReason(doc.getString("removalReason"));

        String removedByStr = doc.getString("removedBy");
        if (removedByStr != null) {
            grant.setRemovedBy(UUID.fromString(removedByStr));
        }

        return grant;
    }
}
