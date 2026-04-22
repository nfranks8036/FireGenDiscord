package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.ModalAction;
import net.noahf.firegen.discord.actions.StringDropdownAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocation;
import net.noahf.firegen.discord.incidents.structure.location.LocationType;
import net.noahf.firegen.discord.incidents.structure.location.Venue;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the "Location" button in the "Edit:" second row.
 */
public class EditLocation implements ButtonAction, StringDropdownAction, ModalAction {

    /**
     * Represents the list of {@link LocationType} as {@link SelectOption Discord SelectOptions} that can be used in
     * a {@link StringSelectMenu}
     */
    private static final List<SelectOption> LOCATION_SELECT_OPTIONS =
            Arrays.stream(LocationType.values())
                    .map(lt ->
                            SelectOption.of(lt.displayName(), lt.name())
                                    .withDescription(lt.getDescription())
                    )
                    .toList();

    /**
     * The name of the command needed to access this class.
     */
    @Override
    public String getName() {
        return "location";
    }

    /**
     * The event that occurs after pressing the 'Location' button in the 'Edit' row. This replies a
     * {@link StringSelectMenu} that allows the user to select the type of location they'd like to update the
     * incident to.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        event.reply("Choose a new location type")
                .setEphemeral(true)
                .setComponents(ActionRow.of(
                        StringSelectMenu.create(this.callbackId(ctx))
                                .addOptions(LOCATION_SELECT_OPTIONS)
                                .build()
                ))
                .complete();
    }

    /**
     * The event that occurs after pressing the appropriate {@link EditLocation#LOCATION_SELECT_OPTIONS Location}
     * select option. This event opens a modal for the user to more granularly define the location of the incident.
     */
    @Override
    public void execute(ActionsContext ctx, StringSelectInteractionEvent event) {
        LocationType type;
        // get(0) because only one option is allowed, so there should never be >0
        String selected = event.getInteraction().getSelectedOptions().get(0).getValue();
        try {
            type = LocationType.valueOf(selected);
        } catch (IllegalArgumentException argumentException) {
            DiscordMessages.error(event, "Unable to find location type by '" + selected + "'");
            return;
        }

        Modal modal = Modal.create(
                        this.callbackId(ctx, type.name()),
                        "Location of " + ctx.getIncident().getFormattedId() + " - " + type.displayName()
                )
                .addComponents(type.getLabels(ctx.getIncident()))
                .build();

        event.replyModal(modal).queue();
    }

    /**
     * The event that occurs after submitting the modal and validating that the information is valid.
     */
    @Override
    public void execute(ActionsContext ctx, ModalInteractionEvent event) {
        IncidentImpl incident = ctx.getIncident();
        LocationType type = LocationType.valueOf(ctx.getParameters().get(0));

        // ------- [ GET VENUE IF SET ] --------
        ModalMapping venueMapping = event.getValue("venue");
        Venue venue = ctx.getManager().getVenueBy(venueMapping != null ? venueMapping.getAsString() : null);

        // ------- [ GET COMMON NAME IF SET ] --------
        ModalMapping commonNameMapping = event.getValue("common-name");
        String commonName = commonNameMapping != null && !commonNameMapping.getAsString().isBlank() ?
                commonNameMapping.getAsString() : null;

        // ------- [ GET CUSTOM LOCATION DATA IF SET ] --------
        List<String> data = this.getData(event);

        // ------- [ COMPLETE AND PACKAGE DATA ] -------
        IncidentLocation location = new IncidentLocation(data, type, commonName, venue);
        incident.setLocation(location);

        String narrative = "Location updated: " + location.format();
        DiscordMessages.selfDestructEdit(event, 5, "The location for this incident was updated to `" + location.format() + "`");

        incident.addContributor(event.getUser().getName());
        incident.addNarrative(event.getUser(), IncidentLogEntryImpl.EntryType.UPDATE, narrative);
        incident.postUpdate();
    }

    /**
     * Gets the actual location data provided by the modals that isn't the {@code common-name} or {@code venue}.
     * @param event the current event
     * @return the list of string data to be interpreted by the {@link IncidentLocation} class at a future date
     */
    @NotNull
    private List<String> getData(ModalInteractionEvent event) {
        List<String> data = new ArrayList<>();
        for (ModalMapping mapping : event.getValues()) {

            // ignore common-name and venue as we just set them above
            if (mapping.getCustomId().equalsIgnoreCase("common-name")
                    || mapping.getCustomId().equalsIgnoreCase("venue")) {
                continue;
            }

            // if the string is blank, then we won't be considering it in our list
            if (mapping.getAsString().isBlank()) {
                continue;
            }

            data.add(mapping.getAsString());
        }

        return data;
    }

    @Override
    public void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(ctx, e); }
    }
}
