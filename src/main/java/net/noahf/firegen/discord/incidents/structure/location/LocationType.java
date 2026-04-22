package net.noahf.firegen.discord.incidents.structure.location;

import lombok.Getter;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

import static net.noahf.firegen.discord.incidents.structure.location.Venue.*;

public enum LocationType {
    ADDRESS(
            "Location",
            "A numeric address. Requires: Street address, including numerics. Allows: Common name, venue.",
            Label.of("Address Numerics", "The numbers representing the address.", TextInput.create("address-numerics", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(2)
                    .setMaxLength(8)
                    .setPlaceholder("Ex: 2800")
                    .build()
            ),
            Label.of("Street Name", "The name of the street the numeric address is on.", TextInput.create("address-street", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(2)
                    .setMaxLength(100)
                    .setPlaceholder("Ex: Commerce St")
                    .build()
            ),
            VENUE_COMPONENT,
            COMMON_NAME_COMPONENT
    ),
    MILE_MARKER(
            "Location",
            "A mile-marker or landmark on a road. Requires: Mile marker/landmark, road name. Allows: Venue.",
            Label.of("Road Name", "The road the call is on. Use 'US-' for US routes and 'I-' for interstates. Add direction of travel.", TextInput.create("milemarker-roadname", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(20)
                    .setPlaceholder("Ex: I-81 NB").build()
            ),
            Label.of("Mile-Marker / Landmark", "The mile-marker or landmark. Add 'MM' before a mile-marker.", TextInput.create("milemarker-landmark", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(20)
                    .setPlaceholder("Ex: MM 114 *OR* Exit 5")
                    .build()
            ),
            VENUE_COMPONENT
    ),
    LATITUDE_LONGITUDE(
            "Location",
            "A latitude and longitude. Requires: Two float values. Allows: Additional information, venue.",
            Label.of("Latitude", "The latitude, in *DECIMAL DEGREES*, of the incident.", TextInput.create("latitudelongitude-latitude", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(10)
                    .setPlaceholder("Ex: 37.197523").build()
            ),
            Label.of("Longitude", "The longitude, in *DECIMAL DEGREES*, of the incident.", TextInput.create("latitudelongitude-longitude", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(10)
                    .setPlaceholder("Ex: -80.395021").build()
            ),
            Label.of("Additional Information", "OPTIONAL: More information on what is at this location.", TextInput.create("latitudelongitude-additional", TextInputStyle.PARAGRAPH)
                    .setRequired(false)
                    .setPlaceholder("Ex: Parking Lot of Blacksburg Transit").build()
            ),
            VENUE_COMPONENT
    ),
    INTERSECTION(
            "Location",
            "An intersection of two roads. Requires: Two or more roads. Allows: Multiple roads.",
            Label.of("Intersection: Road #1", "The first road in the intersection.", TextInput.create("intersection-road1", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Ex: N Main St")
                    .setMaxLength(20)
                    .build()
            ),
            Label.of("Intersection: Road #2", "The second road in the intersection.", TextInput.create("intersection-road2", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Ex: Progress St")
                    .setMaxLength(20)
                    .build()
            ),
            Label.of("Intersection: Road #3", "OPTIONAL: The third road in the intersection.", TextInput.create("intersection-road3", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: McDonald St")
                    .setMaxLength(20)
                    .build()
            ),
            Label.of("Intersection: Road #4", "OPTIONAL: The fourth road in the intersection.", TextInput.create("intersection-road4", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: Winston Ave")
                    .setMaxLength(20)
                    .build()
            )
    ),
    CROSS_STREETS(
            "Cross-streets",
            "Two cross-streets for generic locations. Requires: At least one road. Allows: Multiple roads.",
            Label.of("Cross-street: Road #1", "The primary road in the cross-streets.", TextInput.create("crossstreets-road1", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Ex: N Main St")
                    .setMaxLength(20)
                    .build()
            ),
            Label.of("Cross-street: Road #2", "OPTIONAL: The secondary road in the cross-streets.", TextInput.create("crossstreets-road2", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: Progress St")
                    .setMaxLength(20)
                    .build()
            ),
            Label.of("Cross-street: Road #3", "OPTIONAL: The tertiary road in the cross-streets.", TextInput.create("crossstreets-road3", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: McDonald St")
                    .setMaxLength(20)
                    .build()
            ),
            Label.of("Cross-street: Road #4", "OPTIONAL: The quaternary road in the cross-streets.", TextInput.create("crossstreets-road4", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: Winston Ave")
                    .setMaxLength(20)
                    .build()
            )
    ),
    CUSTOM(
            "Location",
            "Custom text to describe the location if none of the above fit.",
            Label.of("Custom Text", "Enter the custom location type in this box.", TextInput.create("custom-custom", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setMaxLength(200)
                    .build()
            )
    );

    private final @Getter String title;
    private final @Getter String description;
    private final Component[] components;

    LocationType(String locationTitle, String description, Component... components) {
        this.title = locationTitle;
        this.description = description;
        this.components = components;
    }

    public String displayName() {
        return this.name().toUpperCase().replace("_", " ");
    }

    public Label[] getLabels(IncidentImpl incident) {
        Label[] labels = new Label[this.components.length];
        for (int i = 0; i < labels.length; i++) {
            Component component = this.components[i];
            if (component.getUniqueId() == VENUE_ID) {
                labels[i] = incident.getLocation().VENUE;
            } else if (component.getUniqueId() == COMMON_NAME_ID) {
                labels[i] = incident.getLocation().COMMON_NAME;
            } else {
                labels[i] = (Label) component;
            }
        }
        return labels;
    }
}