package net.noahf.firegen.discord.incidents.structure.location;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Venue {
    VIRGINIA_TECH("Blacksburg, VA"),
    TOWN_OF_BLACKSBURG("Blacksburg, VA"),
    TOWN_OF_CHRISTIANSBURG("Christiansburg, VA"),
    MONTGOMERY_COUNTY("Montgomery County, VA");

    public static final String STRING_VENUES = Arrays.stream(Venue.values()).map(Enum::name).collect(Collectors.joining(", "));

    private final @Getter String display;
    Venue(String display) {
        this.display = display;
    }
}