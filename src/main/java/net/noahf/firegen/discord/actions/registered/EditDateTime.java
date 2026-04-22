package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.ModalAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.Time;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents the "Date/Time" button in the "Edit:" first row.
 */
public class EditDateTime implements ButtonAction, ModalAction {

    /**
     * Represents the expected {@code time} input format expected of the user to be matched.
     * See {@link DateTimeFormatter} for more information.
     */
    private static final String TIME_INPUT_FORMAT = "HH:mm:ss";

    /**
     * Represents the expected {@code date} input format expected of the user to be matched.
     * See {@link DateTimeFormatter} for more information.
     */
    private static final String DATE_INPUT_FORMAT = "MM/dd/yyyy";

    /**
     * The name of the command needed to access this class
     */
    @Override
    public String getName() {
        return "datetime";
    }

    /**
     * The event that occurs after pressing the "Date/Time" button in the "Edit" row. This replies a {@link Modal} in
     * Discord for the user to edit the date/time.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        IncidentImpl incident = ctx.getIncident();

        // the reason the following date/time fields can't be static is because they require the current incident's
        // date/time to pre-fill the value.
        TextInput time = TextInput.create("time", TextInputStyle.SHORT)
                .setPlaceholder(TIME_INPUT_FORMAT)
                .setRequired(true)
                .setValue(incident.getTime().format(DateTimeFormatter.ofPattern(TIME_INPUT_FORMAT)))
                .build();

        TextInput date = TextInput.create("date", TextInputStyle.SHORT)
                .setPlaceholder(DATE_INPUT_FORMAT)
                .setRequired(false)
                .setValue(incident.getTime().format(DateTimeFormatter.ofPattern(DATE_INPUT_FORMAT)))
                .build();

        Modal modal = Modal.create(this.callbackId(ctx), "Date/Time of " + incident.getFormattedId())
                .addComponents(
                        Label.of(
                                "Time of Incident",
                                "In the format of (" + TIME_INPUT_FORMAT + ")",
                                time
                        ),
                        Label.of(
                                "Date of Incident",
                                "In the format of (" + DATE_INPUT_FORMAT + ")",
                                date
                        )
                ).build();

        event.replyModal(modal).queue();
    }

    /**
     * The event that occurs after submitting the modal from the event above.
     */
    @Override
    public void execute(ActionsContext ctx, ModalInteractionEvent event) {
        IncidentImpl incident = ctx.getIncident();

        ModalMapping timeMapping = event.getValue("time");
        ModalMapping dateMapping = event.getValue("date");

        if (timeMapping == null) {
            // we place this condition at the top because it's REQUIRED regardless of what is inputted
            DiscordMessages.error(event, "Expected value 'time' to be set in modal, found none.");
            return;
        }

        // LocalTime is always going to be set per the condition above, LocalDate may not be so we will assume
        //   the date to be today.
        LocalTime time = LocalTime.parse(
                timeMapping.getAsString(),
                DateTimeFormatter.ofPattern(TIME_INPUT_FORMAT)
        );
        LocalDate date = incident.getTime().toLocalDate();

        if (dateMapping != null) {
            // the date mapping is OPTIONAL, if not set it will default to the `toLocalDate` mentioned above
            date = LocalDate.parse(dateMapping.getAsString(), DateTimeFormatter.ofPattern(DATE_INPUT_FORMAT));
        }

        incident.setDate(date, time);

        long unix = Time.getUnix(time.atDate(date));
        String narrative = "Changed time to <t:" + unix + ">";
        DiscordMessages.selfDestruct(event, 5, narrative);

        incident.addContributor(event.getUser().getName());
        incident.addNarrative(event.getUser(), IncidentLogEntryImpl.EntryType.UPDATE, narrative);
        incident.postUpdate();
    }




    @Override
    public void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(ctx, e); }
    }
}
