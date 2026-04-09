package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class IncidentLocation {

    private final @Getter String data;
    private final @Getter LocationType type;
    private final @Getter @Nullable String commonName;
    private final @Getter @Nullable String venue;

    public enum LocationType {
        ADDRESS, MILE_MARKER, LATITUDE_LONGITUDE, CROSS_STREETS, CUSTOM
    }

}
