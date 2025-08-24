package xyz.kayaaa.xenon.bukkit.provider;

import com.jonahseguin.drink.argument.CommandArg;
import com.jonahseguin.drink.exception.CommandExitMessage;
import com.jonahseguin.drink.parametric.DrinkProvider;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.RankService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class RankProvider extends DrinkProvider<Rank> {

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
    public Rank defaultNullValue() {
        return null;
    }

    @Nullable
    @Override
    public Rank provide(@Nonnull CommandArg arg, @Nonnull List<? extends Annotation> annotations) throws CommandExitMessage {
        String name = arg.get();
        Rank rank = ServiceContainer.getService(RankService.class).getRank(name);
        if (rank == null) {
            throw new CommandExitMessage("No rank with name '" + name + "'.");
        }
        return rank;
    }

    @Override
    public String argumentDescription() {
        return "rank";
    }

    @Override
    public List<String> getSuggestions(@Nonnull String prefix) {
        final String finalPrefix = prefix.toLowerCase();
        return ServiceContainer.getService(RankService.class).getRanks().stream().map(Rank::getName).filter(s -> finalPrefix.isEmpty() || s.toLowerCase().startsWith(finalPrefix)).collect(Collectors.toList());
    }

}
