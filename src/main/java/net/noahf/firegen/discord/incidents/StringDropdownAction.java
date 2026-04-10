package net.noahf.firegen.discord.incidents;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.noahf.firegen.discord.incidents.structure.Incident;

public interface StringDropdownAction extends FireGenAction {

    void execute(Incident incident, StringSelectInteractionEvent event);

    @Override
    default void execute(Incident incident, GenericInteractionCreateEvent event) {
        this.execute(incident, (StringSelectInteractionEvent) event);
    }

}
