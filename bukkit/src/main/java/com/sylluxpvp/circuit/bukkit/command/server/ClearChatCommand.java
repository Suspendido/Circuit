package com.sylluxpvp.circuit.bukkit.command.server;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("cc|clearchat")
@CommandPermission("circuit.cmd.clearchat")
public class ClearChatCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void clear(CommandSender sender, @Optional @Default("false") boolean ignoreStaff) {
        sender.sendMessage(CC.GREEN + "Clearing chat...");
        for (int i = 0; i < 500; i++) {
            ServerUtils.sendMessage(" ", player -> {
                Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
                if (profile == null) return true;
                if (ignoreStaff) return true;
                return !profile.getCurrentGrant().getData().isStaff();
            });
        }
        ServerUtils.sendMessage("&aThe chat messages were cleared!");
    }
}
