package net.noahf.firegen.discord.incidents.buttonaction;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EditType implements ButtonAction {

    public static final Map<User, Incident> editIncidents = new HashMap<>();

    @Override
    public String getName() {
        return "incidenttype";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {

        editIncidents.put(event.getUser(), incident);
        long destruct = Time.getUnixOffset(11, TimeUnit.SECONDS);
        event.reply("You're now editing the incident " + incident.getFormattedId() + " (" + incident.getType().getCompleteName() + "). "
                + "Type `/set-type <new incident type> <reason>` to change the incident type." +
                "\n\n-# This message will self-destruct <t:" + destruct + ":R>"
        ).setEphemeral(true).complete().deleteOriginal().queueAfter(10, TimeUnit.SECONDS);
    }
}
