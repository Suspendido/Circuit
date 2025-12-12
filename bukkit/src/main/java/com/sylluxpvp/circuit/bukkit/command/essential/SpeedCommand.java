package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("speed|espeed")
@CommandPermission("circuit.command.speed")
public class SpeedCommand extends BaseCommand {

    @Default
    @Syntax("<speed> [player]")
    @CommandCompletion("1|2|3|4|5|6|7|8|9|10 @players")
    public void onSpeed(Player sender, int speed, @Optional Player target) {
        Player player = target != null ? target : sender;

        if (target != null && !sender.hasPermission("circuit.command.speed.others")) {
            sender.sendMessage(CC.translate("&cYou don't have permission to change others' speed."));
            return;
        }

        if (speed < 1) {
            sender.sendMessage(CC.translate("&cSpeed must be at least 1."));
            return;
        }

        if (speed > 10) {
            speed = 10;
        }

        boolean flying = player.isFlying();

        if (flying) {
            player.setFlySpeed(calculateSpeed(speed, true));
        } else {
            player.setWalkSpeed(calculateSpeed(speed, false));
        }

        String type = flying ? "Fly" : "Walk";
        player.sendMessage(CC.translate("&6" + type + " speed set to &f" + speed + "&6."));

        if (target != null && !target.equals(sender)) {
            sender.sendMessage(CC.translate("&6Set " + target.getName() + "'s " + type.toLowerCase() + " speed to &f" + speed + "&6."));
        }
    }

    private float calculateSpeed(int speed, boolean isFly) {
        float defaultSpeed = isFly ? 0.1F : 0.2F;
        float maxSpeed = 1.0F;

        if (speed < 1) {
            return defaultSpeed * speed;
        }

        float ratio = ((float) speed - 1.0F) / 9.0F * (maxSpeed - defaultSpeed);
        return ratio + defaultSpeed;
    }
}
