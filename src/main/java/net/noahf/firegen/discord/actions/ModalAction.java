package net.noahf.firegen.discord.actions;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

/**
 * Represents an action involving a {@link net.dv8tion.jda.api.modals.Modal JDA modal}
 */
public interface ModalAction extends FireGenAction {

    void execute(ActionsContext ctx, ModalInteractionEvent event);

    @Override
    default void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        this.execute(ctx, (ModalInteractionEvent) event);
    }

}
