package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.StringDropdownAction;
import net.noahf.firegen.discord.incidents.structure.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.ListDiff;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents the "Agencies" button in the Edit row.
 */
public class EditAgencies implements ButtonAction, StringDropdownAction {

    /**
     * The name of the command needed to access this class
     */
    @Override
    public String getName() {
        return "agencies";
    }

    /**
     * The event that occurs after pressing the "Agencies" button in the "Edit" row. This replies a String Select Menu
     * to the Discord user that allows them to select agencies they wish to append or remove from the incident.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        event.reply("Choose agencies that are responding.")
                .setEphemeral(true)
                .setComponents(ActionRow.of(StringSelectMenu.create(this.callbackId(ctx))
                        .addOptions(Main.incidents.getAgencies().stream()
                                .map(AgencyImpl::getSelectOption)
                                .limit(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                                .toList()
                        )
                        .setDefaultOptions(
                                ctx.getIncident().getAgencies().stream().map(AgencyImpl::getSelectOption).toList()
                        )
                        .setRequired(true)
                        // the max amount of value IS the amount of values we have available.
                        // i.e., the user can select infinite agencies (attach all agencies)
                        .setMaxValues(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                        .build()
                ))
                .complete();
    }

    /**
     * The event that occurs after clicking away from the {@link StringSelectMenu} from the event above.
     */
    @Override
    public void execute(ActionsContext ctx, StringSelectInteractionEvent event) {
        IncidentImpl incident = ctx.getIncident();

        // we create a new (empty) list entitled agencies that has the new list of all agencies that are selected
        // due to the fact the default values are already set in the event listed above, then we can expect that
        // selected options will only be ones that the user wants attached. therefore we can just assume the
        // list of values are up-to-date and accurate.
        List<AgencyImpl> agencies = new ArrayList<>();
        for (AgencyImpl agency : Main.incidents.getAgencies()) {
            if (!event.getValues().contains(agency.getShorthand())) {
                continue;
            }
            agencies.add(agency);
        }

        // ListDiff will find out which agencies were added and which agencies were removed
        ListDiff<AgencyImpl> diff = ListDiff.compare(incident.getAgencies(), agencies);

        StringJoiner narrative = new StringJoiner(" and ");
        if (!diff.getAdded().isEmpty()) {
            narrative.add("Added agencies " +
                    String.join(", ", diff.getAdded().stream().map(AgencyImpl::getShorthand).toList())
            );
        }
        if (!diff.getRemoved().isEmpty()) {
            narrative.add(
                    // to keep consistent capitalization. if 'getAdded' is empty, then this section will be shown first.
                    (diff.getAdded().isEmpty() ? "R" : "r") + "emoved" +
                            " agencies " +
                            String.join(", ", diff.getRemoved().stream().map(AgencyImpl::getShorthand).toList())
            );
        }

        incident.setAgencies(agencies);

        DiscordMessages.selfDestructEdit(event, 5, narrative.toString());

        incident.addContributor(event.getUser().getName());
        incident.addNarrative(event.getUser(), IncidentLogEntryImpl.EntryType.UPDATE, narrative.toString());
        incident.postUpdate();
    }


    @Override
    public void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(ctx, e); }
    }
}
