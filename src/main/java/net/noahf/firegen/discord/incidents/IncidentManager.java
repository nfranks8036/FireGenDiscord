package net.noahf.firegen.discord.incidents;

import lombok.Getter;
import net.noahf.firegen.discord.incidents.structure.Agency;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentType;
import net.noahf.firegen.discord.incidents.structure.location.Venue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the Incident manager, which creates, modifies, and deletes {@link Incident incidents}, as well as the
 * allowed data that can be put into the incidents, including {@link IncidentType incident types},
 * {@link net.noahf.firegen.discord.incidents.structure.location.IncidentLocation incident locations},
 * and even {@link Venue venues}.
 */
public class IncidentManager {

    /**
     * List of {@link Incident} that are currently ongoing right now. Closed incidents may still show up in this list.
     */
    private final List<Incident> incidents = new ArrayList<>();

    /**
     * The {@code newIncidentType} is the {@link IncidentType} that is used by default when an incident is created with
     * no type assigned to it.<br>
     * Default in some CAD software is {@code >NEW<}
     */
    @Getter IncidentType newIncidentType;

    //<editor-fold desc="Imported with IncidentStructureImporter">
    /**
     * This is the list of allowed {@link IncidentType Incident Types}.
     * These should include every possible variant, including qualifies.
     * This is imported from the {@link IncidentStructureImporter#INCIDENT_TYPE_FILE incident types file}.
     */
    @Getter List<IncidentType> incidentTypes = new ArrayList<>();

    /**
     * This is the list of {@link Agency Agencies} in this system.
     * This is imported from the {@link IncidentStructureImporter#AGENCIES_FILE agencies file}.
     */
    @Getter List<Agency> agencies = new ArrayList<>();

    /**
     * This is the list of allowed {@link Venue Venues}.
     * This is imported from the {@link IncidentStructureImporter#VENUES_FILE venues file}.
     */
    @Getter List<Venue> venues = new ArrayList<>();
    //</editor-fold>

    public IncidentManager() {
        IncidentStructureImporter importer = new IncidentStructureImporter();
        importer.importIncidentTypes(this);
        importer.importAgencies(this);
        importer.importVenues(this);
    }

    /**
     * Find a certain {@link IncidentType} given a string. This will search the
     * {@link IncidentType#getCompleteName() IncidentType's complete name}.
     * @param type the complete name of the incident type to search for.
     * @return the {@link IncidentType} associated with that type, or {@code null} if not found.
     */
    public IncidentType getTypeFromString(String type) {
        for (IncidentType t : this.incidentTypes) {
            if (t.getCompleteName().equalsIgnoreCase(type)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Requests the list of all allowed {@link IncidentType IncidentTypes} and their qualifiers from the manager.
     * @return the list of allowed types
     */
    public List<IncidentType> listAllIncidentTypes() {
        return this.incidentTypes;
    }

    /**
     * Requests a list of all allowed {@link IncidentType IncidentTypes} as {@link String strings} that are
     * autocomplete-ready.
     * @return the list of incident types which are autocomplete ready (can be fed into Discord's autocomplete). Please
     *         note: this <b>DOES NOT</b> limit the amount of results, you will receive the full list!
     */
    public List<String> listAllIncidentTypesForAutocomplete() {
        return this.listAllIncidentTypes().stream().map(IncidentType::getCompleteName).toList();
    }

    /**
     * Creates a new {@link Incident} and adds it to the {@link IncidentManager#incidents list of all active incidents}.
     * Note that this Incident will lack any data.
     * @return a blank Incident class
     */
    public Incident createNewIncident() {
        Incident incident = new Incident(this);
        this.incidents.add(incident);
        return incident;
    }

    /**
     * Retrieves an <b>ACTIVE</b> {@link Incident incident} from the manager by the {@link Incident#getId() ID}.
     * @param id the identifying number after the year in the incident number
     * @return the associated incident with the ID, or {@code null} if it's not found
     */
    public @Nullable Incident getIncidentBy(long id) {
        for (Incident i : this.incidents) {
            if (i.getId() == id) {
                return i;
            }
        }
        return null;
    }

    /**
     * Retrieves an {@link Agency agency} from the manager by the {@link Agency#getShorthand() shorthand name}.
     * @param shorthand the shorthand for the agency name
     * @return the associated agency with that shorthand name, or {@code null} if it's not found
     */
    public @Nullable Agency getAgencyBy(String shorthand) {
        for (Agency a : this.agencies) {
            if (a.getShorthand().equalsIgnoreCase(shorthand)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Retrieves a {@link Venue venue} from the manager by the {@link Venue#getName() normal name}.
     * @param name the name for the venue
     * @return the associated venue with that name, or {@code null} if it's not found
     */
    public @Nullable Venue getVenueBy(String name) {
        if (name == null) {
            return null;
        }

        for (Venue v : this.venues) {
            if (v.getName().equalsIgnoreCase(name)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Retrieves the list of {@link Venue venues} stringified by their name and concatenated with a ", "
     * @return the string form of the venues.
     */
    public String getConcatenatedVenues() {
        return this.venues.stream().map(Venue::getName).collect(Collectors.joining(", "));
    }

}
