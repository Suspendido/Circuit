package com.sylluxpvp.circuit.shared.gift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import com.sylluxpvp.circuit.shared.tools.circuit.Serializable;

import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class GiftCode<T extends Serializable> implements Serializable {

    private UUID UUID;
    private String code;
    private T reward;
    private long createdAt;
    private long duration;
    private boolean revoked;
    private boolean redeemed;
    private UUID redeemedBy;
    private long redeemedAt;

    public boolean isExpired() {
        if (duration == -1) return false;
        return System.currentTimeMillis() > createdAt + duration;
    }

    public boolean isAvailable() {
        if (reward == null) return false;
        return !redeemed && !isExpired() && !isRevoked();
    }

    @Override
    public String getID() {
        return this.UUID.toString();
    }

    @Override
    public String getType() {
        return "gift";
    }

    @Override
    public String getExpiryMessage() {
        return "&cThis gift code has expired" + (this.duration != -1 ? " on " + TimeUtils.formatDate(createdAt + duration) + "!" : "!");
    }

    public String getAlreadyRedeemedMessage() {
        return "&cThis gift code was already redeemed" + (this.redeemedAt != 0 ? " on " + TimeUtils.formatDate(redeemedAt) + "!" : "!");
    }

    @Override
    public String getRemovalMessage() {
        return "&cThis gift code has been revoked!";
    }

    public Document toDocument() {
        Document doc = new Document()
                .append("uuid", UUID.toString())
                .append("code", code)
                .append("createdAt", createdAt)
                .append("duration", duration)
                .append("redeemed", redeemed)
                .append("revoked", revoked)
                .append("redeemedBy", redeemedBy != null ? redeemedBy.toString() : null)
                .append("redeemedAt", redeemedAt);

        if (reward != null) {
            doc.append("rewardType", reward.getType());
            doc.append("rewardID", reward.getID());
            if (!reward.getType().equalsIgnoreCase("rank")) {
                doc.append("rewardContent", reward.toDocument());
            }
        }

        return doc;
    }
}