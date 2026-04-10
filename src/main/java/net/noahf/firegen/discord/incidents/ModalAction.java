package net.noahf.firegen.discord.incidents;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.incidents.structure.Incident;

public interface ModalAction extends FireGenAction {

    void execute(Incident incident, ModalInteractionEvent event);

    @Override
    default void execute(Incident incident, GenericInteractionCreateEvent event) {
        this.execute(incident, (ModalInteractionEvent) event);
    }

}
