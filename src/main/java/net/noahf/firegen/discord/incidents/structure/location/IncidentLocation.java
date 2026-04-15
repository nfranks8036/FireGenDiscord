package net.noahf.firegen.discord.incidents.structure.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.noahf.firegen.discord.Main;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
public class IncidentLocation {

    public IncidentLocation(List<String> custom) {
        this(custom, LocationType.CUSTOM, null, null);
    }

    final Label COMMON_NAME = Label.of("Common Name", "OPTIONAL: The name the general public may know this place by.", TextInput.create("common-name", TextInputStyle.SHORT)
            .setRequired(false)
            .setMaxLength(100)
            .setPlaceholder("Ex: Blacksburg Transit")
            .build()
    );
    final Label VENUE = Label.of("Venue",
            "OPTIONAL: ALLOWED: " + Main.incidents.getConcatenatedVenues().substring(0, Math.min(100 - "OPTIONAL: ALLOWED: ".length(), Main.incidents.getConcatenatedVenues().length())),
            TextInput.create("venue", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder(Main.incidents.getVenues().get(0).getName())
                    .setMaxLength(Main.incidents.getVenues().stream().map(Venue::getName).max(Comparator.comparingInt(String::length)).stream().findFirst().orElse(" ".repeat(100)).length())
                    .setMinLength(Main.incidents.getVenues().stream().map(Venue::getName).min(Comparator.comparingInt(String::length)).stream().findFirst().orElse(" ".repeat(1)).length())
                    .build()
    );

    private @Getter List<String> data;
    private @Getter LocationType type;
    private @Getter @Nullable String commonName;
    private @Getter @Nullable Venue venue;

    public boolean isSet() {
        return this.data != null && !this.data.isEmpty();
    }

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
