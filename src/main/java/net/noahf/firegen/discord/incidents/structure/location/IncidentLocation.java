package net.noahf.firegen.discord.incidents.structure.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class IncidentLocation {

    private @Getter List<String> data;
    private @Getter LocationType type;
    private @Getter @Nullable String commonName;
    private @Getter @Nullable Venue venue;

    public boolean isSet() {
        return this.data != null && !this.data.isEmpty();
    }

    static final Label COMMON_NAME = Label.of("Common Name", "OPTIONAL: The name the general public may know this place by.", TextInput.create("common-name", TextInputStyle.SHORT)
            .setRequired(false)
            .setMaxLength(100)
            .setPlaceholder("Ex: Blacksburg Transit")
            .build()
    );
    static final Label VENUE = Label.of("Venue",
            "OPTIONAL: ALLOWED: " + Venue.STRING_VENUES.substring(0, Math.min(100 - "OPTIONAL: ALLOWED: ".length(), Venue.STRING_VENUES.length())),
            TextInput.create("venue", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder(Venue.TOWN_OF_BLACKSBURG.name())
                    .setMaxLength(Arrays.stream(Venue.values()).map(Enum::name).max(Comparator.comparingInt(String::length)).stream().findFirst().orElse(" ".repeat(100)).length())
                    .setMinLength(Arrays.stream(Venue.values()).map(Enum::name).min(Comparator.comparingInt(String::length)).stream().findFirst().orElse(" ".repeat(1)).length())
                    .build()
    );

    public String format() {
        if (!this.isSet()) {
            return " ";
        }

        String main = (this.commonName != null ? this.commonName + ", " : "");
        switch (type) {
            case ADDRESS -> {
                main = main + data.get(0) + " " + data.get(1);
            }
            case MILE_MARKER -> {
                main = main + data.get(0) + " @ " + data.get(1);
            }
            case INTERSECTION -> {
                main = main + String.join(" / ", data);
            }
            case CROSS_STREETS, LATITUDE_LONGITUDE, CUSTOM -> {
                main = main + String.join(", ", data);
            }
        }
        if (venue != null) {
            main = main + ", " + venue.getDisplay();
        }
        return main;
    }

}
