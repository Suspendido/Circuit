package com.sylluxpvp.circuit.bukkit.command.server;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.service.BukkitChatService;
import com.sylluxpvp.circuit.shared.redis.packets.misc.MessagePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("chat|chatmanager|cm")
@CommandPermission("circuit.cmd.chat")
public class ChatCommand extends BaseCommand {

    @Subcommand("help")
    @CatchUnknown
    @Default
    public void doHelp(CommandSender sender) {
        this.showCommandHelp();
    }

    @Subcommand("toggle")
    @Description("Toggles chat")
    public void toggle(CommandSender sender) {
        ServiceContainer.getService(BukkitChatService.class).toggleChat();
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new MessagePacket(CircuitPlugin.getInstance().getShared().getServer().getName(), "&9Circuit &7» &f" + (ServiceContainer.getService(BukkitChatService.class).isChatEnabled() ? "&aChat was enabled!" : "&cChat was disabled!"), false));
    }

    @Subcommand("setslowdown|slowdown")
    @Description("Sets the chat slowdown (in ms)")
    public void cooldown(CommandSender sender, @Name("slowdown") long slowdown) {
        ServiceContainer.getService(BukkitChatService.class).setSlowdown(slowdown);
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new MessagePacket(CircuitPlugin.getInstance().getShared().getServer().getName(), "&9Circuit &7» &aChat slowdown was set to " + TimeUtils.formatTime(slowdown) + "!", false));
    }

    @Subcommand("filter")
    @Description("Filters/unfilters a word")
    public void filter(CommandSender sender, @Name("word") @Single String word) {
        if (!ServiceContainer.getService(BukkitChatService.class).isFilterEnabled()) {
            sender.sendMessage(CC.RED + "The chat filter is disabled! Enable it using /chat togglefilter.");
            return;
        }

        ServiceContainer.getService(BukkitChatService.class).filterWord(word);
        sender.sendMessage(CC.translate(ServiceContainer.getService(BukkitChatService.class).isFiltered(word) ? "&a\"" + word + "\" has been filtered!" : "&c\"" + word + "\" has been unfiltered!"));
    }

    @Subcommand("togglefilter|tf")
    @Description("Toggles the chat filter")
    public void toggleFilter(CommandSender sender) {
        ServiceContainer.getService(BukkitChatService.class).toggleFilter();
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new MessagePacket(CircuitPlugin.getInstance().getShared().getServer().getName(), "&9Circuit &7» &f" + (ServiceContainer.getService(BukkitChatService.class).isFilterEnabled() ? "&aChat filter was enabled!" : "&cChat filter was disabled! &7(You should still be respectful, though)"), false));
    }
}
