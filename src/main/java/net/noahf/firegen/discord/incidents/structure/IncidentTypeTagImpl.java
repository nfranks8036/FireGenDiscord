package net.noahf.firegen.discord.incidents.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.noahf.firegen.api.incidents.IncidentTypeTag;

import java.util.ArrayList;
import java.util.List;

public class IncidentTypeTagImpl implements IncidentTypeTag {

    public static final IncidentTypeTagImpl DEFAULT;

    static {
        DEFAULT = new IncidentTypeTagImpl(null);
        DEFAULT.name = "None";
        DEFAULT.priorities = new ArrayList<>(List.of("1", "2", "3"));
        DEFAULT.qualifier = null;
    }

    private final JsonObject object;

    public @Getter String name;
    public @Getter List<String> priorities;

    private @Getter Qualifier qualifier;

    public IncidentTypeTagImpl(JsonObject object) {
        this.object = object;
        if (object == null) {
            return;
        }

        this.name = this.object.get("name").getAsString();
        this.priorities = this.object.get("priorities").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();

        if (!this.object.get("qualifiers").isJsonNull()) {

            JsonObject qualifierObj = this.object.get("qualifiers").getAsJsonObject();
            this.qualifier = new Qualifier();
            this.qualifier.required = qualifierObj.get("required").getAsBoolean();
            this.qualifier.unique = qualifierObj.get("unique").getAsBoolean();
            this.qualifier.syntax = qualifierObj.get("syntax").getAsString();
            this.qualifier.qualifiers = qualifierObj.get("list").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();
        } else this.qualifier = null;
    }

    public List<String> fromType(String type) {
        List<String> returned = new ArrayList<>();
        if (qualifier == null) {
            returned.add(type);
            return returned;
        }

        if (!qualifier.required) {
            returned.add(type);
        }

        if (!qualifier.unique) {
            List<String> output = new ArrayList<>();
            int total = 1 << qualifier.qualifiers.size();
            for (int mask = 1; mask < total; mask++) {
                List<String> combo = new ArrayList<>();

                for (int i = 0; i < qualifier.qualifiers.size(); i++) {
                    if ((mask & (1 << i)) != 0) {
                        combo.add(qualifier.qualifiers.get(i));
                    }
                }

                output.add(qualifier.syntax.replace("{T}", type).replace("{Q}", String.join(", ", combo)));
            }

            returned.addAll(output);

            return returned;
        }

        for (String q : qualifier.qualifiers) {
            returned.add(qualifier.syntax.replace("{T}", type).replace("{Q}", q));
        }

        return returned;
    }

    public static class Qualifier {
        private boolean required;
        private boolean unique;
        private String syntax;
        private @Getter List<String> qualifiers;
    }

}
