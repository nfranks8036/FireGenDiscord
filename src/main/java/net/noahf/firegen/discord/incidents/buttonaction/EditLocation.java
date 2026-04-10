package net.noahf.firegen.discord.incidents.buttonaction;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.ModalAction;
import net.noahf.firegen.discord.incidents.StringDropdownAction;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocation;
import net.noahf.firegen.discord.incidents.structure.location.LocationType;
import net.noahf.firegen.discord.incidents.structure.location.Venue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditLocation implements ButtonAction, StringDropdownAction, ModalAction {

    @Override
    public String getName() {
        return "location";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {
        event.reply("Choose a new location type")
                .setEphemeral(true)
                .setComponents(ActionRow.of(
                        StringSelectMenu.create("firegen-" + incident.getId() + "-location")
                                .addOptions(Arrays.stream(LocationType.values()).map(lt ->
                                                SelectOption.of(lt.name().toUpperCase().replace("_", " "), lt.name())
                                                        .withDescription(lt.getDescription())
                                        ).toList())
                                .build()
                ))
                .complete();
    }

    @Override
    public void execute(Incident incident, StringSelectInteractionEvent event) {
        LocationType type;
        String selected = event.getInteraction().getSelectedOptions().get(0).getValue();
        try {
            type = LocationType.valueOf(selected);
        } catch (IllegalArgumentException argumentException) {
            throw new IllegalArgumentException("Unable to find location type by the name '" + selected + "'", argumentException);
        }


        Modal modal = Modal.create("firegen-" + incident.getId() + "-location-" + type.name(), "Location of " + incident.getFormattedId() + " - " + type.name().replace("_", " "))
                .addComponents(type.getLabel())
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void execute(Incident incident, ModalInteractionEvent event) {
        LocationType type = LocationType.valueOf(event.getModalId().split("-")[3]);

        ModalMapping venueMapping = event.getValue("venue");
        Venue venue = null;
        if (venueMapping != null && !venueMapping.getAsString().isBlank()) {
            try {
                venue = Venue.valueOf(venueMapping.getAsString());
            } catch (IllegalArgumentException argumentException) {
                throw new IllegalArgumentException("Venue '" + venueMapping.getAsString() + "' does not exist. Valid venues: " + Arrays.toString(Venue.values()), argumentException);
            }
        }

        ModalMapping commonNameMapping = event.getValue("common-name");
        String commonName = commonNameMapping != null ? commonNameMapping.getAsString() : null;

        List<String> data = new ArrayList<>();
        for (ModalMapping mapping : event.getValues()) {
            if (mapping.getCustomId().equalsIgnoreCase("common-name")
                    || mapping.getCustomId().equalsIgnoreCase("venue")) {
                continue;
            }

            if (mapping.getAsString().isBlank()) {
                continue;
            }

            data.add(mapping.getAsString());
        }

        IncidentLocation location = new IncidentLocation(data, type, commonName, venue);

        event.reply("The location for this incident was updated to `" + location.format() + "`").setEphemeral(true).queue();
        incident.addContributor(event.getUser().getName());

        incident.setLocation(location);
        incident.postUpdate();
    }

    @Override
    public void execute(Incident incident, GenericInteractionCreateEvent event) {
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(incident, e); }
        if (event instanceof  ButtonInteractionEvent e) { this.execute(incident, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(incident, e); }
    }
}
