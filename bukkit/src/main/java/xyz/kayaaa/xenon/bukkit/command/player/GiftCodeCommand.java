package xyz.kayaaa.xenon.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.shared.XenonConstants;
import xyz.kayaaa.xenon.shared.gift.GiftCode;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GiftCodeService;
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;
import xyz.kayaaa.xenon.shared.tools.string.CC;

@CommandAlias("giftcode|gcode|code")
public class GiftCodeCommand extends BaseCommand {

    @Subcommand("create")
    @CommandCompletion("@ranks @times")
    @CommandPermission("xenon.cmd.giftcode")
    public void create(CommandSender sender, @Name("rank") Rank rank, @Name("time") String time) {
        if (rank == null) {
            sender.sendMessage(CC.RED + "This rank doesn't exist!");
            return;
        }
        boolean isPermanent = time.equalsIgnoreCase("perm") || time.equalsIgnoreCase("permanent");
        long duration = isPermanent ? -1 : TimeUtils.parseTime(time);

        if (!isPermanent && !TimeUtils.isTime(time)) {
            sender.sendMessage(CC.translate("&cDuration needs to be a valid time unit."));
            return;
        }

        if (!isPermanent && duration < 0 || duration == Long.MAX_VALUE) {
            sender.sendMessage(CC.translate("&cDuration needs to be a valid time."));
            return;
        }

        GiftCode<Rank> gift = ServiceContainer.getService(GiftCodeService.class).createCode(rank, duration);
        sender.sendMessage(CC.GREEN + CC.translate("Created a gift code for " + rank.getColor() + rank.getName() + "! " + CC.GREEN + "Code: " + gift.getCode()));
    }

    @Subcommand("revoke")
    @CommandCompletion("@giftcodes")
    @CommandPermission("xenon.cmd.giftcode")
    public void revoke(CommandSender sender, @Name("code") String code) {
        GiftCode<Rank> gift = ServiceContainer.getService(GiftCodeService.class).getCode(code);
        if (gift == null) {
            sender.sendMessage(CC.RED + "Invalid gift code. Please re-check the code!");
            return;
        }

        if (gift.isRevoked()) {
            sender.sendMessage(CC.RED + "This gift code is already revoked.");
            return;
        }
        gift.setRevoked(true);
        gift.setRedeemedAt(System.currentTimeMillis());
        gift.setRedeemedBy(XenonConstants.getConsoleUUID());
        sender.sendMessage(CC.GREEN + gift.getCode() + " was revoked successfully.");
    }

    @Default
    @Subcommand("redeem")
    public void redeem(Player sender, @Name("code") String code) {
        GiftCode<Rank> gift = ServiceContainer.getService(GiftCodeService.class).getCode(code);
        if (gift == null) {
            sender.sendMessage(CC.RED + "Invalid gift code. Please re-check your code!");
            sender.sendMessage(CC.GRAY + "NOTE: It is not obligatory for you to include \"XENON-\" in your codes.");
            return;
        }
        if (gift.isRedeemed() && gift.getRedeemedBy().equals(sender.getUniqueId())) {
            sender.sendMessage(CC.RED + "You already redeemed this gift code.");
            return;
        }
        boolean success = ServiceContainer.getService(GiftCodeService.class).redeemCode(sender.getUniqueId(), code);
        if (!success) {
            sender.sendMessage(CC.translate("&cFailed to redeem. " + (gift.isExpired() ? gift.getExpiryMessage() : gift.isRedeemed() ? gift.getAlreadyRedeemedMessage() : gift.isRevoked() ? "This gift code was revoked." : gift.getReward() == null ? "This gift was invalid, with no reward." : "The reason is unknown.")));
            return;
        }

        sender.sendMessage(CC.GREEN + CC.translate("Redeemed! You now have the " + gift.getReward().getColor() + gift.getReward().getName() + " &arank" + (gift.getDuration() == -1 ? "!" : " for " + TimeUtils.formatTime(gift.getDuration()) + "!")));
    }
}
