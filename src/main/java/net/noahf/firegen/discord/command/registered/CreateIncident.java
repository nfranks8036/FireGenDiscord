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
import net.noahf.firegen.discord.incidents.structure.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocation;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the command used to create an incident.
 * {@code /create-incident <type> [date] [time] [location] [agencies]}
 */
public class CreateIncident extends Command {

    /**
     * Represents the {@code date} format that the user must type in order to be parsed correctly.
     * See {@link DateTimeFormatter} for more information.
     */
    private static final String DATE_CREATE_FORMAT = "MM/dd/yyyy";

    /**
     * Represents the {@code time} format that the user must type in order to be parsed correct.
     * See {@link DateTimeFormatter} for more information.
     */
    private static final String TIME_CREATE_FORMAT = "HH:mm";

    public CreateIncident() {
        super("create-incident", "Creates an incident to put in radio activity.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "type",
                                        "REQUIRED: The type of incident.",
                                        true, true
                                ),
                                new OptionData(OptionType.STRING, "time",
                                        "The time (" + TIME_CREATE_FORMAT +") of the incident, will default" +
                                                " to today if no 'date' field is set.",
                                        false, false
                                ),
                                new OptionData(OptionType.STRING, "date",
                                        "The date (" + DATE_CREATE_FORMAT + ") of the incident, MUST set " +
                                                "a 'time' field if this field is set.",
                                        false, false
                                ),
                                new OptionData(OptionType.STRING, "location",
                                        "The location of the incident.",
                                        false, false
                                ),
                                new OptionData(OptionType.STRING, "agencies",
                                        "Any agencies attached, separated with a comma.",
                                        false, true
                                )
                        })
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        IncidentImpl incident = Main.incidents.createNewIncident();

        // ---------- incident type ----------
        OptionMapping typeOption = event.getOption("type");
        if (typeOption != null) {
            incident.setType(typeOption.getAsString());
        }

        // ---------- incident location ----------
        OptionMapping locationOption = event.getOption("location");
        if (locationOption != null) {
            incident.setLocation(new IncidentLocation(List.of(locationOption.getAsString())));
        }

        // ---------- incident agencies ----------
        OptionMapping agenciesOption = event.getOption("agencies");
        if (agenciesOption != null) {
            // remove whitespace from the agencies string, we don't care if they put a space or not after a comma
            String agenciesString = agenciesOption.getAsString().replaceAll("\\s+", "");

            String[] agenciesList = agenciesString.split(",");

            List<AgencyImpl> agencies = new ArrayList<>();
            for (String agencyString : agenciesList) {
                // required syntax of command is the shorthand. e.g., "BFD,BVRS,SUP5,BPD,VTPD"
                AgencyImpl a = Main.incidents.getAgencyBy(agencyString);
                if (a == null) continue;

                agencies.add(a);
            }
            incident.setAgencies(agencies);
        }

        // ---------- incident date and time ----------
        OptionMapping dateOption = event.getOption("date");
        OptionMapping timeOption = event.getOption("time");
        LocalDate date = LocalDate.now(); // default to now if no date provided
        LocalTime time = LocalTime.now(); // default to now if no time provided
        if (timeOption != null) {

            String timeString = timeOption.getAsString();
            try {
                time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern(TIME_CREATE_FORMAT));
            } catch (DateTimeParseException e) {
                DiscordMessages.error(event,
                        "Failed to parse your time, expected format '" + TIME_CREATE_FORMAT + "', " +
                                "got '" + timeString + "'", e
                );
                return;
            }

        }

        if (dateOption != null) {
            if (timeOption == null) {
                DiscordMessages.error(event, "You must set a time if you also select a date.");
                return;
            }

            String dateString = dateOption.getAsString();
            try {
                date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_CREATE_FORMAT));
            } catch (DateTimeParseException e) {
                DiscordMessages.error(event, "Failed to parse your date, expected format '" +
                        DATE_CREATE_FORMAT + "', got '" + dateString + "'", e
                );
                return;
            }
        }
        incident.setDate(date, time);

        // ---------- incident contributors // begin list ----------
        incident.addContributor(event.getUser().getName());

        // ---------- post update for first time to channels ----------
        incident.postUpdate();

        DiscordMessages.selfDestruct(event, 5,
                "Created new incident with those details. Check an admin channel for more information."
        );
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        return switch (focused.getName()) {
            case "type" ->
                // send the list of all motor vehicle crashes
                Main.incidents.listAllIncidentTypesForAutocomplete();

            case "agencies" -> {
                // the format for the agencies parameter is: <AGENCY 1>,<AGENCY 2>,<AGENCY 3> (e.g., BFD,BVRS,SUP5)
                // we are attempting to have autocomplete minimic this format to the best of its ability
                // -------- [ BELOW THIS LINE CONTAINS SOME LLM-WRITTEN OR MODIFIED CODE ] --------

                // remove whitespace - we do not care about it
                String input = event.getFocusedOption().getValue().replaceAll("\\s+", "");

                // the input for the agencies field will only take in the shorthands (e.g., 'SUP5' not 'Supervisor 5')
                List<String> allAgencies = Main.incidents.getAgencies().stream().map(AgencyImpl::getShorthand).toList();

                String[] parts = input.split(",");
                List<String> selected = new ArrayList<>();

                String currentToken;
                if (parts.length > 0) {
                    for (int i = 0; i < parts.length - 1; i++) {
                        selected.add(parts[i].trim().toUpperCase());
                    }
                    currentToken = parts[parts.length - 1].trim().toUpperCase();
                } else {
                    // this is required to make sure it's effectively final for the lambda in the return 'filter' coming up
                    // we can't just initialize it and change it :(
                    currentToken = "";
                }

                List<String> available = allAgencies.stream()
                        .filter(a -> !selected.contains(a))
                        .map(s -> s + ",")
                        .toList();

                // yield = return in a switch statement for those who didn't know (I didn't)
                yield available.stream()
                        .filter(a -> a.startsWith(currentToken))
                        .map(a -> {
                            String suggestion;
                            if (selected.isEmpty()) {
                                suggestion = a;
                            } else {
                                suggestion = String.join(",", selected) + "," + a;
                            }
                            return suggestion;
                        })
                        .limit(25)
                        .toList();
                // -------- [ ABOVE THIS LINE CONTAINS SOME LLM-WRITTEN OR MODIFIED CODE ] --------
            }

            default -> null;
        };
    }
}
