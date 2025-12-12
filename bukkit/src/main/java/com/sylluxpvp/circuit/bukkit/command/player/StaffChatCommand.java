package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.chat.ChatChannel;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("sc|staffchat")
@CommandPermission("circuit.staffchat")
public class StaffChatCommand extends BaseCommand {

    @Default
    @Description("Enters/leaves staff chat")
    public void onStaffChat(Player sender) {
        Profile profile = ServiceContainer.getService(ProfileService.class).find(sender.getUniqueId());
        if (profile == null) return;

        profile.setChannel(profile.getChannel() == ChatChannel.DEFAULT ? ChatChannel.STAFF : ChatChannel.DEFAULT);
        sender.sendMessage(CC.translate(profile.getChannel() == ChatChannel.STAFF ? "&aYou have joined staff chat!" : "&cYou have left staff chat!"));
    }
}