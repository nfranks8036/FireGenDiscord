package net.noahf.firegen.discord.incidents.buttonaction;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.utilities.Log;

public class EditType implements ButtonAction {

    @Override
    public String getName() {
        return "incidenttype";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {
        event.reply("The type for this incident must be edited with /set-type. For the incident ID, enter `" + incident.getFormattedId() + "`").setEphemeral(true).queue();
    }
}
