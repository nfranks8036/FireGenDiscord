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
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocation;
import net.noahf.firegen.discord.incidents.structure.location.LocationType;
import net.noahf.firegen.discord.utilities.Time;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CreateIncident extends Command {

    public CreateIncident() {
        super("create-incident", "Creates an incident to put in radio activity.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "type", "REQUIRED: The type of incident.", true, true),
                                new OptionData(OptionType.STRING, "time", "The time (HH:mm) of the incident, will default to today if no 'date' field is set.", false, false),
                                new OptionData(OptionType.STRING, "date", "The date (MM/dd/yyyy) of the incident, MUST set a 'time' field if this field is set.", false, false),
                                new OptionData(OptionType.STRING, "location", "The location of the incident.", false, false),
                                new OptionData(OptionType.STRING, "agencies", "Any agencies attached, separated with a comma.", false, true)
                        })
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        OptionMapping typeOption = event.getOption("type");
        Incident incident = Main.incidents.createNewIncident();
        if (typeOption != null) {
            incident.setType(typeOption.getAsString());
        }
        OptionMapping locationOption = event.getOption("location");
        if (locationOption != null) {
            incident.setLocation(new IncidentLocation(List.of(locationOption.getAsString()), LocationType.CUSTOM, null, null));
        }

        OptionMapping dateOption = event.getOption("date");
        OptionMapping timeOption = event.getOption("time");
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        if (timeOption != null) {
            time = LocalTime.parse(timeOption.getAsString(), DateTimeFormatter.ofPattern("HH:mm"));
        }

        if (dateOption != null) {
            if (timeOption == null) {
                // set earlier, so we can ignore this and throw the error only
                return;
            }
            date = LocalDate.parse(dateOption.getAsString(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }
        incident.setDate(date, time);

        incident.addContributor(event.getUser().getName());

        long destruct = Time.getUnixOffset(6, TimeUnit.SECONDS);
        event.reply("Created new incident with those details. Check an admin channel for more information." +
                "\n\n-# This message will self-destruct <t:" + destruct + ":R>"
        ).setEphemeral(true).complete().deleteOriginal().queueAfter(5, TimeUnit.SECONDS);

        incident.postUpdate();
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("type")) {
            return Main.incidents.listAllIncidentTypesForAutocomplete();
        }
        if (focused.getName().equalsIgnoreCase("agencies")) {
            return List.of("test");
        }
        return null;
    }
}
