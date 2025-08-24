package xyz.kayaaa.xenon.shared.service.impl;

import lombok.Getter;
import lombok.NonNull;
import org.bson.Document;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.tools.Serializable;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.NoActionService;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;

import java.util.UUID;

public class GrantService extends NoActionService {

    @Getter
    private final UUID console = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override @NonNull
    public String getIdentifier() {
        return "grant";
    }

    public Grant<Rank> createGrant(Rank rank, UUID author, long duration, String reason) {
        Grant<Rank> grant = new Grant<>(UUID.randomUUID(), rank, author);
        grant.setDuration(duration);
        grant.setReason(reason);
        grant.setTimeCreated(System.currentTimeMillis());
        return grant;
    }

    public Grant<Rank> createGrant(Rank rank, UUID author, String reason) {
        Grant<Rank> grant = new Grant<>(UUID.randomUUID(), rank, author);
        grant.setDuration(-1);
        grant.setReason(reason);
        grant.setTimeCreated(System.currentTimeMillis());
        return grant;
    }

    public Grant<Rank> getDefaultGrant() {
        Grant<Rank> grant = new Grant<>(UUID.randomUUID(), ServiceContainer.getService(RankService.class).getDefaultRank(), console);
        grant.setDuration(-1);
        grant.setReason("Default Grant");
        grant.setTimeCreated(System.currentTimeMillis());
        return grant;
    }

    public Grant<? extends Serializable> fromDocument(Document doc) {
        UUID uuid = UUID.fromString(doc.getString("uuid"));
        UUID author = UUID.fromString(doc.getString("author"));

        Serializable data = null;
        String dataType = doc.getString("dataType");
        if (dataType != null) {
            if (dataType.equalsIgnoreCase("rank")) {
                UUID rankID = null;
                try {
                    rankID = UUID.fromString(doc.getString("dataContent"));
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
