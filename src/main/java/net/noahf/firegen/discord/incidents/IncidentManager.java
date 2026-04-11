package net.noahf.firegen.discord.incidents;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.noahf.firegen.discord.incidents.buttonaction.*;
import net.noahf.firegen.discord.incidents.buttonaction.narrative.AddNarrative;
import net.noahf.firegen.discord.incidents.buttonaction.narrative.HideNarrative;
import net.noahf.firegen.discord.incidents.structure.Agency;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentType;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IncidentManager {

    private final List<Incident> incidents;

    @Getter IncidentType newIncidentType;
    @Getter List<IncidentType> incidentTypes;
    @Getter List<Agency> agencies;

    private @Getter List<FireGenAction> actions;

    public IncidentManager() {
        this.incidentTypes = new ArrayList<>();
        this.incidents = new ArrayList<>();
        this.agencies = new ArrayList<>();
        this.actions = new ArrayList<>();

        this.actions.add(new EditType());
        this.actions.add(new EditDateTime());
        this.actions.add(new EditLocation());
        this.actions.add(new EditAgencies());
        this.actions.add(new AddNarrative());
        this.actions.add(new HideNarrative());
        this.actions.add(new ChangeStatus());

        IncidentStructureImporter importer = new IncidentStructureImporter();
        importer.importIncidentTypes(this);
        importer.importAgencies(this);
    }

    public IncidentType getTypeFromString(String type) {
        for (IncidentType t : this.incidentTypes) {
            if (t.getCompleteName().equalsIgnoreCase(type)) {
                return t;
            }
        }
        return null;
    }

    public List<IncidentType> listAllIncidentTypes() {
        return this.incidentTypes;
    }

    public List<String> listAllIncidentTypesForAutocomplete() {
        return this.listAllIncidentTypes().stream().map(IncidentType::getCompleteName).toList();
    }

    public Incident createNewIncident() {
        Incident incident = new Incident(this);
        this.incidents.add(incident);
        return incident;
    }

    public @Nullable Incident getIncidentBy(long id) {
        for (Incident i : this.incidents) {
            if (i.getId() == id) {
                return i;
            }
        }
        return null;
    }

    public @Nullable Agency getAgencyByShorthand(String shorthand) {
        for (Agency a : this.agencies) {
            if (a.getShorthand().equalsIgnoreCase(shorthand)) {
                return a;
            }
        }
        return null;
    }

    public void processAction(GenericInteractionCreateEvent event, String incidentId, String command) {
        Incident incident = this.getIncidentBy(Long.parseLong(incidentId));
        if (incident == null) {
            throw new IllegalArgumentException("Incident with ID '" + incidentId + "' does not exist.");
        }

        Log.info("Searching for command '" + command + "'");
        for (FireGenAction action : this.actions) {
            if (!action.getName().equalsIgnoreCase(command)) {
                continue;
            }
            action.execute(incident, event);
            return;
        }
        throw new IllegalStateException("No command found for '" + command + "'");
    }

}
