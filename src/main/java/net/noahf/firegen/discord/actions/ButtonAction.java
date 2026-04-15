package net.noahf.firegen.discord.actions;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * Represents an action involving a {@link net.dv8tion.jda.api.components.buttons.Button JDA button}
 */
public interface ButtonAction extends FireGenAction {

    void execute(ActionsContext ctx, ButtonInteractionEvent event);

    @Override
    default void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        this.execute(ctx, (ButtonInteractionEvent) event);
    }
}
