package net.noahf.firegen.discord.incidents.structure;

import lombok.Getter;
import lombok.Setter;
import net.noahf.firegen.discord.utilities.Time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class IncidentLogEntryImpl {

    private static final String NARRATIVE_TIME_FORMAT = "HH:mm";

    private final @Getter long id;
    private final @Getter LocalDateTime time;
    private final @Getter long userId;
    private final @Getter String entry;

    private @Getter @Setter EntryType type;

    IncidentLogEntryImpl(LocalDateTime time, long userId, String entry, EntryType type) {
        this.id = new Random(System.currentTimeMillis()).nextLong(1000000, 9999999);
        this.time = time;
        this.userId = userId;
        this.entry = entry.toUpperCase()
                .strip()
                .replace("\n", "") // don't allow newLine characters
                .replace("*", "\\*") // remove Discord formatting involving *
                .replace("_", "\\_"); // remove Discord formatting involving _
        this.type = type;
    }

    public enum EntryType {
        UPDATE(false), NARRATIVE(true), HIDDEN(true);

        private final @Getter boolean editable;
        EntryType(boolean editable) {
            this.editable = editable;
        }
    }

    public String formatReceiver() {
        return "`" + this.time.format(DateTimeFormatter.ofPattern(NARRATIVE_TIME_FORMAT)) + "` " + entry;
    }

    public String formatAdmin() {
        return "<t:" + Time.getUnix(this.time) + ":T> `"+ type.name() + "` <@" + userId + "> " + entry;
    }

}