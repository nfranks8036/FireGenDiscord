package net.noahf.firegen.discord.incidents.discordtasks;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.incidents.DiscordTask;
import net.noahf.firegen.discord.incidents.structure.Incident;

public class EditType implements DiscordTask {

    @Override
    public String getName() {
        return "type";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {

    }
}
