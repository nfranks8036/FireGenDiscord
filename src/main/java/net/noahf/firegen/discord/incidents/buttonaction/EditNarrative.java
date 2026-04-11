package net.noahf.firegen.discord.incidents.buttonaction;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.Incident;

public class EditNarrative implements ButtonAction {

    @Override
    public String getName() {
        return "narrative";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {

    }

}
