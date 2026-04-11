package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.buttonaction.EditType;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentNarrativeEntry;
import net.noahf.firegen.discord.incidents.structure.IncidentType;
import net.noahf.firegen.discord.utilities.ErrorEmbed;
import net.noahf.firegen.discord.utilities.Time;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SetType extends Command {

    public SetType() {
        super("set-type", "Sets the type of an incident. Press 'Edit Type' on an incident to start editing.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "new-type", "The new incident type for this incident.", true, true),
                                new OptionData(OptionType.STRING, "reason", "Changed call type due to... [complete the sentence]", true, false),
                        })
                        .finish());
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        Incident incident = EditType.editIncidents.get(event.getUser());
        if (incident == null) {
            event.replyEmbeds(ErrorEmbed.error("You are not currently editing an incident. Press 'Edit Type' to edit the incident type.")).queue();
            return;
        }

        String oldType = incident.getType().getCompleteName();

        OptionMapping typeOption = event.getOption("new-type");
        OptionMapping reasonOption = event.getOption("reason");
        if (typeOption == null || reasonOption == null) {
            event.replyEmbeds(ErrorEmbed.error("Expected argument 'new-type' and 'reason' to be set, found one to not be set.")).queue(); return;
        }

        String type = typeOption.getAsString().toUpperCase();
        String reason = reasonOption.getAsString();
        if (reason.startsWith("hide:")) {
            reason = reason.substring("hide:".length());
        }

        incident.setType(type);
        incident.addContributor(event.getUser().getName());
        incident.postUpdate();

        if (!reasonOption.getAsString().startsWith("hide:")) {
            incident.addNarrative(event.getUser(), IncidentNarrativeEntry.EntryType.UPDATE,
                    "Changed incident type from '" + oldType + "' to '" + type + "' due to " + reason
            );
        }

        long destruct = Time.getUnixOffset(6, TimeUnit.SECONDS);
        event.reply("Set incident type to `" + type + "` due to '" + reason + "'" +
                "\n\n-# This message will self-destruct <t:" + destruct + ":R>"
        ).setEphemeral(true).complete().deleteOriginal().queueAfter(5, TimeUnit.SECONDS);
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("new-type")) {
            return Main.incidents.listAllIncidentTypesForAutocomplete();
        }
        return null;
    }
}
