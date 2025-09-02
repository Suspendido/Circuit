package xyz.kayaaa.xenon.bukkit.command.server;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import xyz.kayaaa.xenon.bukkit.XenonPlugin;
import xyz.kayaaa.xenon.shared.redis.packets.misc.MessagePacket;
import xyz.kayaaa.xenon.shared.tools.string.CC;

@CommandAlias("broadcast|bc|alert")
@CommandPermission("xenon.cmd.broadcast")
public class BroadcastCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void broadcast(CommandSender sender, @Name("message") @Flags("remaining") String message) {
        XenonPlugin.getInstance().getShared().getRedis().sendPacket(new MessagePacket(null, "&9Xenon &7» &f" + message, false));
        sender.sendMessage(CC.translate("&aBroadcasted to the whole network!"));
    }

}
