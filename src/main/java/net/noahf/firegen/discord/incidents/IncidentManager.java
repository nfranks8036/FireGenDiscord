package net.noahf.firegen.discord.incidents;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.noahf.firegen.discord.command.registered.CreateIncident;
import net.noahf.firegen.discord.incidents.buttonaction.EditDateTime;
import net.noahf.firegen.discord.incidents.buttonaction.EditLocation;
import net.noahf.firegen.discord.incidents.buttonaction.EditType;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentType;
import net.noahf.firegen.discord.incidents.structure.IncidentTypeTag;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocation;
import net.noahf.firegen.discord.incidents.structure.location.LocationType;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

public class IncidentManager {

    private static final String INCIDENT_TYPE_FILE = "incident_types.json";

    private final List<Incident> incidents;

    private @Getter IncidentType newIncidentType;
    private @Getter List<IncidentType> incidentTypes;

    private @Getter List<FireGenAction> actions;

    public IncidentManager() {
        this.incidentTypes = new ArrayList<>();
        this.incidents = new ArrayList<>();
        try
                (InputStream input = CreateIncident.class.getClassLoader().getResourceAsStream(INCIDENT_TYPE_FILE))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + INCIDENT_TYPE_FILE + "' to exist, found none.");
            }
            JsonObject object = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();
            List<IncidentTypeTag> tags = new ArrayList<>();
            IncidentTypeTag newTag = null;
            for (JsonElement element : object.getAsJsonArray("tags").asList()) {
                IncidentTypeTag tag = new IncidentTypeTag(element.getAsJsonObject());
                tags.add(tag);
                if (tag.name.equalsIgnoreCase("NEW_INCIDENT")) {
                    newTag = tag;
                }
            }
            if (newTag == null) {
                throw new IllegalStateException("Expected an incident tag with name 'NEW_INCIDENT', found none.");
            }

            for (JsonElement element : object.getAsJsonArray("types").asList()) {
                JsonObject obj = element.getAsJsonObject();
                String name = obj.get("name").getAsString();
                String tagStr = obj.get("tag").getAsString();
                IncidentTypeTag tag = tags.stream().filter(itt -> itt.name.equalsIgnoreCase(tagStr)).findFirst().orElse(null);
                if (tag == null) {
                    throw new IllegalStateException("Expected type '" + name + "' to have an associated 'tag'");
                }
                List<IncidentType> types = new ArrayList<>();
                if (newIncidentType == null && tagStr.equalsIgnoreCase("NEW_INCIDENT")) {
                    newIncidentType = new IncidentType(name, tag, 0);
                    types.add(newIncidentType);
                } else if (tag.getQualifier() == null) {
                    types.add(new IncidentType(name, tag, 0));
                } else {
                    List<String> stringTags = tag.fromType(name);
                    for (int i = 0; i < stringTags.size(); i++) {
                        types.add(new IncidentType(name, tag, i));
                    }
                }

                incidentTypes.addAll(types);
            }
            if (newIncidentType == null) {
                throw new IllegalStateException("Expected an incident type to be tagged 'NEW_INCIDENT', found none.");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }

        this.actions = new ArrayList<>();
        this.actions.add(new EditType());
        this.actions.add(new EditDateTime());
        this.actions.add(new EditLocation());
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

    public List<String> listAllIncidentTypesNamed() {
        return this.listAllIncidentTypes().stream().map(IncidentType::getCompleteName).toList();
    }

    public Incident createNewIncident() {
        Incident incident = new Incident(
                this,
                new Random(System.currentTimeMillis()).nextLong(1000000, 9999999),
                this.getNewIncidentType(),
                new ArrayList<>(),
                new IncidentLocation(new ArrayList<>(), LocationType.CUSTOM, null, null),
                LocalDateTime.now(),
                new ArrayList<>()
        );
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
