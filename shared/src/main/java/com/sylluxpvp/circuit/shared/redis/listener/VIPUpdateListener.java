package com.sylluxpvp.circuit.shared.redis.listener;

import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.packets.vip.VIPUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class VIPUpdateListener extends PacketListener<VIPUpdatePacket> {

    @Override
    public void listen(VIPUpdatePacket packet) {
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        if (profileService == null) return;

        Profile profile = profileService.find(packet.getPlayerUUID());
        if (profile == null) return;

        profile.setVipStatus(packet.isVipStatus());
    }
}
