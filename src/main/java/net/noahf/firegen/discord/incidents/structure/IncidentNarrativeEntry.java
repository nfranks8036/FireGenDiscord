package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
public class IncidentNarrativeEntry {

    private final @Getter LocalDateTime time;
    private final @Getter String entry;
    private final @Getter EntryType type;

    public enum EntryType {
        INIT, STATUS, UPDATE, INFO, STAGE, CHANGE, CANCEL, REQUEST, HAZARD
    }

}
