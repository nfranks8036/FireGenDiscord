package net.noahf.firegen.discord.incidents.buttonaction.narrative;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.ModalAction;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentNarrativeEntry;
import net.noahf.firegen.discord.utilities.DiscordMessages;

/**
 * Represents the "Add" button next to the "Narrative:" row
 */
public class AddNarrative implements ButtonAction, ModalAction {

    /**
     * Represents the text input field in the modal for the add narrative
     */
    private static final TextInput TEXT_INPUT =
            TextInput.create("text", TextInputStyle.SHORT)
            .setRequiredRange(5, 256)
                .setRequired(true)
                .setPlaceholder("Add narrative text here...")
                .build();

    /**
     * The name of the command
     */
    @Override
    public String getName() {
        return "addnarrative";
    }

    /**
     * The initial button press on the admin dashboard.
     * This opens the modal for the user to type in their narrative
     */
    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {
        Modal modal = Modal.create(this.callbackId(incident), "Add Narrative to " + incident.getFormattedId())
                .addComponents(Label.of("Narrative Text", "Will be automatically converted to uppercase.", TEXT_INPUT))
                .build();

        event.replyModal(modal).queue();
    }

    /**
     * This event occurs when the user presses the complete button on the modal.
     */
    @Override
    public void execute(Incident incident, ModalInteractionEvent event) {
        ModalMapping textMapping = event.getValue("text");
        if (textMapping == null) {
            DiscordMessages.error(event, "You must input some text to add to the narrative!");
            return;
        }

        incident.addNarrative(event.getUser(), IncidentNarrativeEntry.EntryType.NARRATIVE, textMapping.getAsString());

        DiscordMessages.noMessageEdit(event);
        incident.addContributor(event.getUser().getName());

        incident.postUpdate();
    }

    @Override
    public void execute(Incident incident, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(incident, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(incident, e); }
    }
}
