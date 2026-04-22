package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentStatus;
import net.noahf.firegen.discord.utilities.DiscordMessages;

/**
 * Represents the "Close Incident" or "Re-open Incident" buttons in the Status row.
 */
public class ChangeStatus implements ButtonAction {

    /**
     * The name of the command needed to access this class
     */
    @Override
    public String getName() {
        return "status";
    }

    /**
     * The event that occurrs after pressing the 'Close Incident' or 'Re-open Incident'
     * buttons. This flip-flops the status of the incident from
     * {@link IncidentStatus#CLOSED CLOSED} to either {@link IncidentStatus#PENDING PENDING} or
     * {@link IncidentStatus#ACTIVE ACTIVE} and vice versa
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        IncidentImpl incident = ctx.getIncident();

        incident.setStatus(incident.getStatus().opposite(incident));
        IncidentStatus newStatus = incident.getStatus();

        switch (newStatus) {
            case PENDING, ACTIVE -> {
                incident.addNarrative(event.getUser(), IncidentLogEntryImpl.EntryType.UPDATE, "Incident re-opened");
            }
            case CLOSED, TIMED_OUT -> {
                incident.addNarrative(event.getUser(), IncidentLogEntryImpl.EntryType.UPDATE, "Incident closed");
            }
        }

        DiscordMessages.noMessage(event);

        incident.addContributor(event.getUser().getName());
        incident.postUpdate();
    }

}
