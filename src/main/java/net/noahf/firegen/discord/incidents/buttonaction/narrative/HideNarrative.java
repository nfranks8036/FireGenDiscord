package net.noahf.firegen.discord.incidents.buttonaction.narrative;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.StringDropdownAction;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentNarrativeEntry;
import net.noahf.firegen.discord.utilities.ErrorEmbed;
import net.noahf.firegen.discord.utilities.Time;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class HideNarrative implements ButtonAction, StringDropdownAction {

    @Override
    public String getName() {
        return "hidenarrative";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {
        if (incident.getOutputNarrative().isEmpty()) {
            event.replyEmbeds(ErrorEmbed.error("There is no narrative text to hide.\nNote: You cannot remove sections of the narrative not labelled 'NARRATIVE'")).setEphemeral(true).queue();
            return;
        }

        event.reply("Select which narrative item to hide.\nHiding narrative text is essentially deleting it for the end users, though it will remain stored.")
                .setEphemeral(true).setComponents(ActionRow.of(

                StringSelectMenu.create("firegen-" + incident.getId() + "-hidenarrative")
                        .addOptions(
                                incident.getOutputNarrative().stream()
                                        .map(ine ->
                                                SelectOption.of(ine.getTime().format(DateTimeFormatter.ofPattern("MMM d, yyyy @ HH:mm:ss")), String.valueOf(ine.getId()))
                                                        .withDescription(ine.getEntry().substring(0, Math.min(100, ine.getEntry().length())))
                                        )
                                        .toList()
                        )
                        .build()
        )).queue();
    }

    @Override
    public void execute(Incident incident, StringSelectInteractionEvent event) {
        if (event.getValues().isEmpty()) {
            event.replyEmbeds(ErrorEmbed.error("Expected at least one option selected for hide narrative, found none.")).queue();
            return;
        }

        String id = event.getValues().get(0);
        IncidentNarrativeEntry entry = incident.getNarrative().stream().filter(ine -> String.valueOf(ine.getId()).equalsIgnoreCase(id)).findFirst().orElse(null);;
        if (entry == null) {
            event.replyEmbeds(ErrorEmbed.error("There is no narrative entry found with id `" + id + "`")).queue();
            return;
        }

        entry.setType(IncidentNarrativeEntry.EntryType.HIDDEN);
        incident.injectNarrative(entry);

        long destruct = Time.getUnixOffset(6, TimeUnit.SECONDS);
        event.editMessage("Hidden the narrative entry `" + entry.getEntry() + "`.\nNote that this does not fully delete it, only hide it from end users." +
                        "\n\n-# This message will self-destruct <t:" + destruct + ":R>")
                .setComponents(new ArrayList<>()).complete().deleteOriginal().queueAfter(5, TimeUnit.SECONDS);

        incident.addContributor(event.getUser().getName());

        incident.postUpdate();
    }

    @Override
    public void execute(Incident incident, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(incident, e); }
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(incident, e); }
    }
}
