package net.noahf.firegen.discord.actions;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

/**
 * Represents an action involving a {@link StringDropdownAction JDA dropdown action}
 */
public interface StringDropdownAction extends FireGenAction {

    void execute(ActionsContext ctx, StringSelectInteractionEvent event);

    @Override
    default void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        this.execute(ctx, (StringSelectInteractionEvent) event);
    }

}
