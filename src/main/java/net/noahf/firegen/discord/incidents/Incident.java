package net.noahf.firegen.discord.incidents;

import lombok.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.noahf.firegen.discord.Main;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.util.List;
import java.util.Random;
import java.util.SimpleTimeZone;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Incident {

    private final IncidentManager manager;
    private final @Getter long id;

    private @Getter @NotNull IncidentType type;
    private @Getter final List<Agency> agencies;
    private @Getter @NotNull IncidentLocation location;
    private @Getter @NotNull LocalDateTime time;

    private final @Getter List<IncidentNarrativeEntry> narrative;

    private long messageId;


    public Incident setType(String type) {
        IncidentType newType = manager.listAllIncidentTypes().get(type);
        if (newType == null) {
            throw new IllegalArgumentException("Expected a valid incident type from file, got '" + type + "'");
        }

        this.type = newType;
        return this;
    }

    public void setTime(LocalTime time) {
        this.setDate(LocalDate.now(), time);
    }

    public void setDate(LocalDate date, LocalTime time) {
        this.time = time.atDate(date);
    }

    public void setLocation(String location, IncidentLocation.LocationType type) {
        this.location = new IncidentLocation(location, type);
    }

    public void addAgency(Agency agency) {
        this.agencies.add(agency);
    }

    public void removeAgency(Agency agency) {
        if (!this.agencies.remove(agency)) {
            throw new IllegalArgumentException("Agency '" + agency + "' is not in the incident '" + this + "'");
        }
    }

    public void post(TextChannel channel) {
        channel.sendMessage(this.format()).queue();
    }

    public MessageCreateData format() {
        long unix = this.time.toEpochSecond(ZoneOffset.UTC);
        return MessageCreateData.fromContent(String.format(
                """
                        # :rotating_light: %s :rotating_light:
                        [<t:%d:d>@<t:%d:T> // <t:%d:R>]
                        
                        **Responding:** %s
                        **Location:** %s
                        
                        **Narrative:**
                        %s
                        """,
                this.type,
                unix, unix, unix,
                String.join(", ", this.agencies.stream().map(Agency::getAgencyText).toList()),
                this.location.getData(),
                String.join(", ", this.narrative.stream().map(IncidentNarrativeEntry::getEntry).toList())
        ));
    }

}