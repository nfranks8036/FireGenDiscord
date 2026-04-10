package net.noahf.firegen.discord.incidents;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.incidents.structure.Incident;

public interface ButtonAction extends FireGenAction {

    void execute(Incident incident, ButtonInteractionEvent event);

    @Override
    default void execute(Incident incident, GenericInteractionCreateEvent event) {
        this.execute(incident, (ButtonInteractionEvent) event);
    }
}
