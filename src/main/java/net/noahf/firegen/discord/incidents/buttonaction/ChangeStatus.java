package net.noahf.firegen.discord.incidents.buttonaction;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentNarrativeEntry;
import net.noahf.firegen.discord.incidents.structure.IncidentStatus;
import net.noahf.firegen.discord.utilities.Time;

import java.util.concurrent.TimeUnit;

public class ChangeStatus implements ButtonAction {

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {
        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(incident.getStatus().opposite(incident));
        IncidentStatus newStatus = incident.getStatus();

        switch (newStatus) {
            case PENDING, ACTIVE -> {
                incident.addNarrative(event.getUser(), IncidentNarrativeEntry.EntryType.UPDATE, "Incident re-opened");
            }
            case CLOSED, TIMED_OUT -> {
                incident.addNarrative(event.getUser(), IncidentNarrativeEntry.EntryType.UPDATE, "Incident closed");
            }
        }

        long destruct = Time.getUnixOffset(6, TimeUnit.SECONDS);
        event.reply("The status of this incident was changed to `" + newStatus + "` from `" + oldStatus + "`" +
                "\n\n-# This message will self-destruct <t:" + destruct + ":R>"
        ).setEphemeral(true).complete().deleteOriginal().queueAfter(5, TimeUnit.SECONDS);
        incident.addContributor(event.getUser().getName());

        incident.postUpdate();
    }

}
