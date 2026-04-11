package net.noahf.firegen.discord.incidents.buttonaction;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.StringDropdownAction;
import net.noahf.firegen.discord.incidents.structure.Agency;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentNarrativeEntry;
import net.noahf.firegen.discord.utilities.ListDiff;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class EditAgencies implements ButtonAction, StringDropdownAction {

    @Override
    public String getName() {
        return "agencies";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {
        event.reply("Choose agencies that are responding.")
                .setEphemeral(true)
                .setComponents(ActionRow.of(
                        StringSelectMenu.create("firegen-" + incident.getId() + "-agencies")
                                .addOptions(Main.incidents.getAgencies().stream().map(Agency::getSelectOption).limit(25).toList())
                                .setDefaultOptions(incident.getAgencies().stream().map(Agency::getSelectOption).toList())
                                .setRequired(true)
                                .setMaxValues(25)
                                .build()
                ))
                .complete();
    }

    @Override
    public void execute(Incident incident, StringSelectInteractionEvent event) {
        List<Agency> agencies = new ArrayList<>();
        for (Agency agency : Main.incidents.getAgencies()) {
            if (event.getValues().contains(agency.getShorthand())) {
                agencies.add(agency);
            }
        }

        ListDiff<Agency> diff = ListDiff.compare(incident.getAgencies(), agencies);
        Log.info(diff.toString());

        StringJoiner narrative = new StringJoiner(" and ");
        if (!diff.getAdded().isEmpty()) {
            narrative.add("Added agencies " + String.join(", ", diff.getAdded().stream().map(Agency::getShorthand).toList()));
        }
        if (!diff.getRemoved().isEmpty()) {
            narrative.add((diff.getAdded().isEmpty() ? "R" : "r") + "emoved agencies " + String.join(", ", diff.getRemoved().stream().map(Agency::getShorthand).toList()));
        }
        long destruct = Time.getUnixOffset(6, TimeUnit.SECONDS);
        event.editMessage(narrative.toString() +
                "\n\n-# This message will self-destruct <t:" + destruct + ":R>"
        ).setComponents(new ArrayList<>()).complete().deleteOriginal().queueAfter(5, TimeUnit.SECONDS);

        incident.setAgencies(agencies);
        incident.addContributor(event.getUser().getName());
        incident.addNarrative(event.getUser(), IncidentNarrativeEntry.EntryType.UPDATE, narrative.toString());

        incident.postUpdate();
    }


    @Override
    public void execute(Incident incident, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(incident, e); }
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(incident, e); }
    }
}
