package com.jonahseguin.drink.provider.spigot;

import com.jonahseguin.drink.argument.CommandArg;
import com.jonahseguin.drink.exception.CommandExitMessage;
import com.jonahseguin.drink.parametric.DrinkProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class OfflinePlayerProvider extends DrinkProvider<OfflinePlayer> {

    private final Plugin plugin;

    public OfflinePlayerProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean doesConsumeArgument() {
        return true;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean allowNullArgument() {
        return false;
    }

    @Nullable
    @Override
    public OfflinePlayer defaultNullValue() {
        return null;
    }

    @Nullable
    @Override
    public OfflinePlayer provide(@Nonnull CommandArg arg, @Nonnull List<? extends Annotation> annotations) throws CommandExitMessage {
        String name = arg.get();
        OfflinePlayer p = plugin.getServer().getOfflinePlayer(name);
        if (p != null) {
            return p;
        }
        throw new CommandExitMessage("No player was found with name '" + name + "'.");
    }

    @Override
    public String argumentDescription() {
        return "offlinePlayer";
    }

    @Override
    public List<String> getSuggestions(@Nonnull String prefix) {
        final String finalPrefix = prefix;
        return plugin.getServer().getOnlinePlayers().stream().map(HumanEntity::getName).filter(s -> finalPrefix.length() == 0 || s.startsWith(finalPrefix)).collect(Collectors.toList());
    }
}
