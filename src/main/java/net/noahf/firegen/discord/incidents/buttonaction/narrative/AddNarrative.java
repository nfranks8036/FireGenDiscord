package net.noahf.firegen.discord.incidents.buttonaction.narrative;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.ModalAction;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentNarrativeEntry;
import net.noahf.firegen.discord.utilities.ErrorEmbed;

import java.time.format.DateTimeFormatter;

public class AddNarrative implements ButtonAction, ModalAction {

    @Override
    public String getName() {
        return "addnarrative";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {
        TextInput text = TextInput.create("text", TextInputStyle.SHORT)
                .setMinLength(5)
                .setMaxLength(256)
                .setRequired(true)
                .setPlaceholder("Add narrative text here...")
                .build();

        Modal modal = Modal.create("firegen-" + incident.getId() + "-addnarrative", "Add Narrative to " + incident.getFormattedId())
                .addComponents(Label.of("Narrative Text", "Will be automatically converted to uppercase.", text))
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void execute(Incident incident, ModalInteractionEvent event) {
        ModalMapping textMapping = event.getValue("text");
        if (textMapping == null) {
            event.replyEmbeds(ErrorEmbed.error("You must input some text to add to the narrative!")).setEphemeral(true).queue();
            return;
        }

        incident.addNarrative(event.getUser(), IncidentNarrativeEntry.EntryType.NARRATIVE, textMapping.getAsString());

        event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();
        incident.addContributor(event.getUser().getName());

        incident.postUpdate();
    }

    @Override
    public void execute(Incident incident, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(incident, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(incident, e); }
    }
}
