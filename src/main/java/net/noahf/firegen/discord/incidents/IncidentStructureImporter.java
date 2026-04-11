package net.noahf.firegen.discord.incidents;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.discord.command.registered.CreateIncident;
import net.noahf.firegen.discord.incidents.structure.Agency;
import net.noahf.firegen.discord.incidents.structure.IncidentType;
import net.noahf.firegen.discord.incidents.structure.IncidentTypeTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IncidentStructureImporter {

    private static final String INCIDENT_TYPE_FILE = "incident_types.json";
    private static final String AGENCIES_FILE = "agencies.json";

    public void importIncidentTypes(IncidentManager manager) {
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(INCIDENT_TYPE_FILE))
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
                if (manager.newIncidentType == null && tagStr.equalsIgnoreCase("NEW_INCIDENT")) {
                    manager.newIncidentType = new IncidentType(name, tag, 0);
                    types.add(manager.newIncidentType);
                } else if (tag.getQualifier() == null) {
                    types.add(new IncidentType(name, tag, 0));
                } else {
                    List<String> stringTags = tag.fromType(name);
                    for (int i = 0; i < stringTags.size(); i++) {
                        types.add(new IncidentType(name, tag, i));
                    }
                }

                manager.incidentTypes.addAll(types);
            }
            if (manager.newIncidentType == null) {
                throw new IllegalStateException("Expected an incident type to be tagged 'NEW_INCIDENT', found none.");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

    public void importAgencies(IncidentManager manager) {
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(AGENCIES_FILE))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + AGENCIES_FILE + "' to exist, found none.");
            }
            JsonArray array = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonArray();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();
                String shorthand = object.get("short").getAsString();
                String longhand = object.get("long").getAsString();
                String format = object.get("format").getAsString();
                String emoji = object.get("emoji").getAsString();

                manager.agencies.add(new Agency(
                        shorthand, longhand, format, emoji,
                        SelectOption.of(format, shorthand)
                                .withDescription("(" + shorthand + ") " + longhand)
//                                .withEmoji(Emoji.fromCustom(emoji, 0, false))
                ));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

}
