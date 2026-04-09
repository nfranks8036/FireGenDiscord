package net.noahf.firegen.discord.incidents;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.incidents.structure.Incident;

public interface DiscordTask {

    String getName();

    void execute(Incident incident, ButtonInteractionEvent event);

}
