package xyz.kayaaa.xenon.bukkit.command;

import lombok.Getter;

@Getter
public class CommandBase {

    private final String name;
    private final String[] aliases;
    private final boolean defaultHelp;

    public CommandBase(String n, String... a) {
        this.name = n;
        this.aliases = a;
        this.defaultHelp = true;
    }

    public CommandBase(String n) {
        this.name = n;
        this.aliases = new String[]{};
        this.defaultHelp = true;
    }

    public CommandBase(String n, boolean defaultHelp) {
        this.name = n;
        this.aliases = new String[]{};
        this.defaultHelp = defaultHelp;
    }

    public CommandBase(String n, boolean defaultHelp, String... a) {
        this.name = n;
        this.aliases = a;
        this.defaultHelp = true;
    }

}
