package xyz.kayaaa.xenon.bukkit.command.impl.player;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.command.CommandBase;
import xyz.kayaaa.xenon.bukkit.menus.ChatColorMenu;
import xyz.kayaaa.xenon.shared.chat.ChatChannel;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;
import xyz.kayaaa.xenon.shared.tools.string.CC;

public class StaffChatCommand extends CommandBase {

    public StaffChatCommand() {
        super("staffchat", "sc");
    }

    @Command(name = "", desc = "Enters/leaves staff chat")
    @Require("xenon.staff.chat")
    public void staff(@Sender Player sender) {
        Profile profile = ServiceContainer.getService(ProfileService.class).find(sender.getUniqueId());
        if (profile == null) return;

        profile.setChannel(profile.getChannel() == ChatChannel.DEFAULT ? ChatChannel.STAFF : ChatChannel.DEFAULT);
        sender.sendMessage(CC.translate(profile.getChannel() == ChatChannel.STAFF ? "&aYou have joined staff chat!" : "&cYou have left staff chat!"));
    }

}
