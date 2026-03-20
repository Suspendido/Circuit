package com.sylluxpvp.circuit.shared.redis.listener;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.redis.packets.rank.RankUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.RankService;

public class RankUpdateListener extends PacketListener<RankUpdatePacket> {

    @Override
    public void listen(RankUpdatePacket packet) {
        RankService rankService = ServiceContainer.getService(RankService.class);
        if (rankService == null) return;

        Rank existingRank = rankService.getRank(packet.getRankUUID());

        if (packet.isDeleted()) {
            if (existingRank != null) {
                rankService.getRanks().remove(existingRank);
            }
            return;
        }

        // Reload rank from database
        Document doc = rankService.getRanksCollection()
                .find(Filters.eq("uuid", packet.getRankUUID().toString()))
                .first();

        if (doc == null) return;

        Rank updatedRank = rankService.fromDocument(doc);

        if (existingRank != null) {
            // Update existing rank properties
            existingRank.setPrefix(updatedRank.getPrefix());
            existingRank.setSuffix(updatedRank.getSuffix());
            existingRank.setColor(updatedRank.getColor());
            existingRank.setWeight(updatedRank.getWeight());
            existingRank.setStaff(updatedRank.isStaff());
            existingRank.setHidden(updatedRank.isHidden());
            existingRank.setPurchasable(updatedRank.isPurchasable());
            existingRank.setDefaultRank(updatedRank.isDefaultRank());
            existingRank.setPermissions(updatedRank.getPermissions());
            existingRank.setInheritances(updatedRank.getInheritances());
            rankService.notifyRankUpdate(existingRank);
        } else {
            // Add new rank
            rankService.getRanks().add(updatedRank);
        }
    }
}
