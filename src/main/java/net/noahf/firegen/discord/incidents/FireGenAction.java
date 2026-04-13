package net.noahf.firegen.discord.incidents;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.noahf.firegen.discord.incidents.structure.Incident;

public interface FireGenAction {

    String getName();

    void execute(Incident incident, GenericInteractionCreateEvent event);

    default String callbackId(Incident incident) {
        return incident.createInteractionIdString(this.getName());
    }

}
