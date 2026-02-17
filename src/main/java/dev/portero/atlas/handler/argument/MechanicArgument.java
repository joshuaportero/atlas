package dev.portero.atlas.handler.argument;

import dev.portero.atlas.mechanic.Mechanic;
import dev.portero.atlas.mechanic.MechanicManager;
import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import org.bukkit.command.CommandSender;

public class MechanicArgument extends ArgumentResolver<CommandSender, Mechanic> {

    private final MechanicManager mechanicManager;

    public MechanicArgument(MechanicManager mechanicManager) {
        this.mechanicManager = mechanicManager;
    }

    @Override
    protected ParseResult<Mechanic> parse(Invocation<CommandSender> invocation,
                                          Argument<Mechanic> context,
                                          String argument) {
        Mechanic mechanic = this.mechanicManager.getMechanic(argument);
        if (mechanic == null) {
            return ParseResult.failure("No mechanic found with name: " + argument);
        }
        return ParseResult.success(mechanic);
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation,
                                    Argument<Mechanic> argument,
                                    SuggestionContext context) {
        return this.mechanicManager.getRegisteredMechanics().stream().collect(SuggestionResult.collector());
    }
}
