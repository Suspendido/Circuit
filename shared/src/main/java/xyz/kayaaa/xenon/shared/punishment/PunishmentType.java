package xyz.kayaaa.xenon.shared.punishment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import xyz.kayaaa.xenon.shared.tools.string.CC;

import java.text.MessageFormat;
import java.util.Arrays;

@RequiredArgsConstructor
@Getter
@AllArgsConstructor
public enum PunishmentType {
    KICK(1, "kicked", "&cYou were kicked from the server!\n&cReason: {0}\n\n&cContact staff if you think this is a mistake."),
    MUTE(2, "muted", "&cYou are muted from the server!\n\n&cReason: {0}\n&cExpires: {1}\n\n&cContact staff if you think this is a mistake."),
    BAN(3, "banned", "&cYou are banned from the server.\n\n&cReason: {0}\n&cExpires: {1}\n\n&cContact staff if you think this is a mistake."),
    BLACKLIST(4, "blacklisted", "&cYou are blacklisted from the server.\n\n&cReason: {0}\n&cExpires: {1}\n\n&cContact staff if you think this is a mistake.", "&cYou are blacklisted from the server.\n&cYour account has relation to \"{0}\" which is blacklisted!\n\n&cReason: {1}\n&cExpires: {2}\n\n&cContact staff if you think this is a mistake.");

    private final int priority;
    private final String action;
    private final String message;
    private String relationMessage;

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
