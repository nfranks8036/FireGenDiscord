package net.noahf.firegen.discord.incidents;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class IncidentLocation {

    private final @Getter String data;
    private final @Getter LocationType type;

    public enum LocationType {
        ADDRESS, MILE_MARKER, LATITUDE_LONGITUDE, CROSS_STREETS, CUSTOM
    }

}
