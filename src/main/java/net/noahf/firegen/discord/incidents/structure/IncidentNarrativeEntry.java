package net.noahf.firegen.discord.incidents.structure;

import lombok.Getter;
import net.noahf.firegen.discord.utilities.Time;

import java.time.LocalDateTime;

public class IncidentNarrativeEntry {

    private final @Getter LocalDateTime time;
    private final @Getter long userId;
    private final @Getter String entry;
    private final @Getter EntryType type;

    IncidentNarrativeEntry(LocalDateTime time, long userId, String entry, EntryType type) {
        this.time = time;
        this.userId = userId;
        this.entry = entry.toUpperCase().strip().replace("\n", "");
        this.type = type;
    }

    public enum EntryType {
        UPDATE, NARRATIVE, HIDDEN
    }

    public String formatReceiver() {
        return "<t:" + Time.getUnix(this.time) + ":T>| " + entry;
    }

    public String formatAdmin() {
        return "<t:" + Time.getUnix(this.time) + ":T> `"+ type.name() + "` <@" + userId + "> " + entry;
    }

}
