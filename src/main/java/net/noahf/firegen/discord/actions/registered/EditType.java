package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the "Type" button in the "Edit" row
 */
public class EditType implements ButtonAction {

    /**
     * Represents the list of users that are currently editing an incident and which incident they're currently editing.
     */
    public static final Map<User, IncidentImpl> editIncidents = new HashMap<>();

    /**
     * The command name required to access this class.
     */
    @Override
    public String getName() {
        return "incidenttype";
    }

    /**
     * The event that occurs after pressing the 'Type' button in the 'Edit' row. This is not only informational but does
     * set a value so the user can edit this incident. It creates a gateway to the `/set-type` command.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        IncidentImpl incident = ctx.getIncident();

        editIncidents.put(event.getUser(), incident);
        DiscordMessages.selfDestruct(event, 10,
                "You're now editing the incident " +
                        incident.getFormattedId() + " (" + incident.getType().getCompleteName() + "). "
                + "Type `/set-type <new-type> <reason>` to change the incident type."
        );
    }
}
