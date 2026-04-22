package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.registered.EditType;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentTypeImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.util.List;

/**
 * Represents the command used to change the {@link IncidentTypeImpl} of an
 * {@link IncidentImpl}.
 * {@code /set-type <new-type> <reason>}
 */
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
        IncidentImpl incident = EditType.editIncidents.get(event.getUser());

        if (incident == null) {
            DiscordMessages.error(event, "You are not currently editing an incident. " +
                    "Press 'Edit Type' of the incident of your choice to edit the incident type."
            );
            return;
        }

        // store old type for the narrative in the future
        String oldType = incident.getType().getCompleteName();

        OptionMapping typeOption = event.getOption("new-type");
        OptionMapping reasonOption = event.getOption("reason");
        if (typeOption == null || reasonOption == null) {
            // this realistically should not happen because the 'required' flag is set on these Options for Discord
            // but this prevents compiler warnings AND YOU CAN NEVER BE TOO SURE
            DiscordMessages.error(event, "Expected argument 'new-type' and 'reason' to be set, " +
                    "found one to not be set."
            );
            return;
        }

        String type = typeOption.getAsString().toUpperCase(); // all call types should be uppercase to match CAD
        String reason = reasonOption.getAsString();
        boolean hiddenFromNarrative = reason.startsWith("hide:");

        // we do not need to worry about removing 'hide:' from the narrative as it will not used or saved anyways

        incident.setType(type);
        incident.addContributor(event.getUser().getName()); // they have contributed

        if (!hiddenFromNarrative) { // if not hidden
            incident.addNarrative(event.getUser(), IncidentLogEntryImpl.EntryType.UPDATE,
                    "Changed incident type from '" + oldType + "' to '" + type + "' due to " + reason
            );
        }

        // post update only after the narrative option has been added
        incident.postUpdate();

        DiscordMessages.selfDestruct(event, 5,
                "Set incident type to `" + type + "` due to '" + reason + "'"
        );
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("new-type")) {
            return Main.incidents.listAllIncidentTypesForAutocomplete();
        }

        return null;
    }
}
