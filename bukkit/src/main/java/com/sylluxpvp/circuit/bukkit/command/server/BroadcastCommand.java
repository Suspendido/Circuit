package com.sylluxpvp.circuit.bukkit.command.server;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.redis.packets.misc.MessagePacket;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("broadcast|bc|alert")
@CommandPermission("circuit.cmd.broadcast")
public class BroadcastCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void broadcast(CommandSender sender, @Name("message") @Flags("remaining") String message) {
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new MessagePacket(null, "&9Circuit &7» &f" + message, false));
        sender.sendMessage(CC.translate("&aBroadcasted to the whole network!"));
    }

}
