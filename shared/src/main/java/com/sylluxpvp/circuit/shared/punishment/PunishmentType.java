package com.sylluxpvp.circuit.shared.punishment;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.text.MessageFormat;
import java.util.Arrays;

@Getter
public enum PunishmentType {
    KICK(1, "kicked", "&cHaz sido kickeado del servidor!\n&cKickeado por&7: &f{0}"),
    MUTE(2, "muted", "&cHaz sido muteado por &4{0}&c.\n&cMuteado por&7: {1}\n&8Muteado Incorrectamente?&7Puedes apelar tu mute\n&7en &9mine.lc/discord"),
    BAN(3, "banned", "&cTu cuenta ha sido baneada.\n\n&cExpira en&7: &f{1}\n&cBaneado por&7: &f{0}\n\n&cPuedes apelar el baneo en nuestro discord &8- &7(&9mine.lc/discord&7)"),
    BANIP(4, "bannedip", "&cTu cuenta ha sido baneada de IP.\n\n&cExpira en&7: &f{1}\n&cBaneado por&7: &f{0}\n\n&cEste ban no es apelable.", "&cTu cuenta ha sido baneada de IP.\n&cTu cuenta esta vinculada con la cuenta \"{0}\" por lo que esta baneada de IP!\n\n&cExpira en&7: &f{2}\n&cBaneado por&7: &f{1}\n\n&cEste ban no es apelable.\n&cGracias por haber jugado al servidor."),
    BLACKLIST(5, "blacklisted", "&cTu cuenta a sido blacklisteada.\n\n&cExpira en&7: &f{1}\n&cBaneado por&7: &f{0}\n\n&cEste ban no es apelable.", "&cTu cuenta ha sido blacklisteada.\n&cTu cuenta esta vinculada con la cuenta \"{0}\" por lo que esta blacklisteada!\n\n&cExpira en&7: &f{2}\n&cBaneado por&7: &f{1}\n\n&cEste ban no es apelable.\n&cGracias por haber jugado al servidor.");

    private final int priority;
    private final String action;
    private final String message;
    private String relationMessage;

    PunishmentType(int priority, String action, String message) {
        this.priority = priority;
        this.action = action;
        this.message = message;
    }

    PunishmentType(int priority, String action, String message, String relationMessage) {
        this.priority = priority;
        this.action = action;
        this.message = message;
        this.relationMessage = relationMessage;
    }

    public String format(String... toFormat) {
        return new MessageFormat(CC.translate(this.message)).format(toFormat);
    }

    public String formatRelation(String... toFormat) {
        if (this.relationMessage == null) return null;
        return new MessageFormat(CC.translate(this.relationMessage)).format(toFormat);
    }

    public String prettyName() {
        return StringUtils.capitalize(this.name().toLowerCase());
    }

    public static PunishmentType from(String string) {
        return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(string)).findFirst().orElse(null);
    }
}
