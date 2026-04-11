package net.noahf.firegen.discord.incidents.buttonaction.narrative;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.Incident;

public class RemoveNarrative implements ButtonAction {

    @Override
    public String getName() {
        return "removenarrative";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {

    }
}
