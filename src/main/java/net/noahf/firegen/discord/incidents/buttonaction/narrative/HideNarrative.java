package net.noahf.firegen.discord.incidents.buttonaction.narrative;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.StringDropdownAction;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentNarrativeEntry;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.ListDiff;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;

import java.io.StringReader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the "Hide" button in the Narrative row
 */
public class HideNarrative implements ButtonAction, StringDropdownAction {

    /**
     * Represents the date and time format that will be shown in the string select menu.
     * See {@link DateTimeFormatter} for format characters
     */
    private static final DateTimeFormatter DATE_TIME_NARRATIVE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy @ HH:mm:ss");

    /**
     * The {@link Function} used to convert the {@link IncidentNarrativeEntry} into a JDA {@link SelectOption}.
     * This function is used every time that the string select option menu is created.
     */
    private static final Function<IncidentNarrativeEntry, SelectOption> CONVERT_TO_SELECT_OPTION =
            (entry) ->
                    SelectOption.of(entry.getTime().format(DATE_TIME_NARRATIVE_FORMAT), String.valueOf(entry.getId()))
                            .withDescription(
                                    // cannot exceed DESCRIPTION_MAX_LENGTH  characters in discord string description
                                    // so we either use the string length and cut off there or
                                    // go to the maximum of DESCRIPTION_MAX_LENGTH characters
                                    entry.getEntry().substring(0,
                                            Math.min(SelectOption.DESCRIPTION_MAX_LENGTH, entry.getEntry().length())
                                    )
                            )
                            .withDefault(entry.getType() == IncidentNarrativeEntry.EntryType.HIDDEN);

    /**
     * The name of the command needed to access this class
     */
    @Override
    public String getName() {
        return "hidenarrative";
    }

    /**
     * The initial button press on the admin dashboard.
     * This replies a string select menu where the user can select which narrative to hide.
     */
    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {
        List<IncidentNarrativeEntry> narrative = incident.getEditableNarrative();
        if (narrative.isEmpty()) {
            DiscordMessages.error(event, "There is no narrative text to hide.\n" +
                    "Note: You cannot remove sections of the narrative not labelled 'NARRATIVE'");
            return;
        }

        ActionRow row = ActionRow.of(StringSelectMenu.create(this.callbackId(incident))
                .addOptions(narrative.stream().map(CONVERT_TO_SELECT_OPTION).toList())
                .setMaxValues(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                .setMinValues(0)
                .build());

        event.reply("Select which narrative item to hide. De-select any narratives to unhide.\n" +
                        "Hiding narrative text is only hides from end users, the text will remain stored.")
                .setEphemeral(true).setComponents(row).queue();
    }

    /**
     * The event occurs after the user has selected a specific string selection option on which narrative item to hide.
     */
    @Override
    public void execute(Incident incident, StringSelectInteractionEvent event) {

        List<String> values = event.getValues();
        int hidden = 0, shown = 0;
        for (IncidentNarrativeEntry entry : incident.getEditableNarrative()) {
            boolean unconfirmedChanges = false;
            IncidentNarrativeEntry.EntryType type = entry.getType();

            if (
                    type == IncidentNarrativeEntry.EntryType.NARRATIVE &&
                    values.contains(String.valueOf(entry.getId()))) {
                // this means that the user has requested this entry to be hidden but it's currently viewable

                hidden++; unconfirmedChanges = true;
                entry.setType(IncidentNarrativeEntry.EntryType.HIDDEN);

            } else if (
                    type == IncidentNarrativeEntry.EntryType.HIDDEN &&
                    !values.contains(String.valueOf(entry.getId()))) {
                // this means that the user has requested to show this entry but it's currently hidden

                shown++; unconfirmedChanges = true;
                entry.setType(IncidentNarrativeEntry.EntryType.NARRATIVE);

            }

            if (!unconfirmedChanges)
                // we don't want to take up resources injecting a narrative that has not changes.
                // this is especially noticeable if we have long narratives
                continue;

            incident.injectNarrative(entry);
        }

        DiscordMessages.selfDestructEdit(event, 5,
                "You have " +
                        (hidden > 0 ? "hidden " + hidden + " narrative entries " : "") +
                        (hidden > 0 && shown > 0 ? "and " : "") +
                        (shown > 0 ? "revealed " + shown + " narrative entries " : "") +
                        "for the end user."
        );

        incident.addContributor(event.getUser().getName());
        incident.postUpdate();
    }

    @Override
    public void execute(Incident incident, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(incident, e); }
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(incident, e); }
    }
}
