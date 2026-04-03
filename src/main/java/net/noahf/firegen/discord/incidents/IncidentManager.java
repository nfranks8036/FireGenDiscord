package net.noahf.firegen.discord.incidents;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.noahf.firegen.discord.command.registered.CreateIncident;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

public class IncidentManager {

    private static final String INCIDENT_TYPE_FILE = "incident_types.json";

    private IncidentType newIncidentType;
    private @Getter List<IncidentType> incidentTypes;

    public IncidentManager() {
        this.incidentTypes = new ArrayList<>();
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
                IncidentType type = new IncidentType(name, tag, name);
                if (newIncidentType == null && tagStr.equalsIgnoreCase("NEW_INCIDENT")) {
                    newIncidentType = type;
                }

                incidentTypes.add(type);
            }
            if (newIncidentType == null) {
                throw new IllegalStateException("Expected an incident type to be tagged 'NEW_INCIDENT', found none.");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

    public Map<String, IncidentType> listAllIncidentTypes() {
        Map<String, IncidentType> returned = new HashMap<>();
        for (IncidentType type : incidentTypes) {
            for (String str : type.getTag().fromType(type.getIncidentType())) {
                returned.put(str, type);
            }
        }
        return returned;
    }

    public Incident createIncident() {
        return new Incident(
                this,
                new Random(System.currentTimeMillis()).nextLong(1000000, 9999999),
                newIncidentType,
                new ArrayList<>(),
                new IncidentLocation(" ", IncidentLocation.LocationType.CUSTOM),
                LocalDateTime.now(),
                new ArrayList<>()
        );
    }

}
