package net.noahf.firegen.discord.incidents;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class IncidentNarrativeEntry {

    private final @Getter String entry;
    private final @Getter EntryType type;

    public enum EntryType {
        INIT, STATUS, UPDATE, INFO, STAGE, CHANGE, CANCEL, REQUEST, HAZARD
    }

}
