package net.noahf.firegen.discord.incidents.structure;

import lombok.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.IncidentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Incident {

    public static Incident createNew() {
        return new Incident(
                Main.incidents,
                new Random(System.currentTimeMillis()).nextLong(1000000, 9999999),
                Main.incidents.getNewIncidentType(),
                new ArrayList<>(),
                new IncidentLocation(" ", IncidentLocation.LocationType.CUSTOM, null, null),
                LocalDateTime.now(),
                new ArrayList<>()
        );
    }

    private final IncidentManager manager;
    private final @Getter long id;

    private @Getter @NotNull IncidentType type;
    private @Getter final List<Agency> agencies;
    private @Getter @NotNull IncidentLocation location;
    private @Getter @NotNull LocalDateTime time;

    private final @Getter List<IncidentNarrativeEntry> narrative;

    private List<Long> messageIds = new ArrayList<>();
    private List<String> contributors = new ArrayList<>();

    public void setType(String type) {
        IncidentType newType = manager.getTypeFromString(type);
        if (newType == null) {
            throw new IllegalArgumentException("Expected a valid incident type from file, got '" + type + "'");
        }

        this.type = newType;
    }

    public void setTime(LocalTime time) {
        this.setDate(LocalDate.now(), time);
    }

    public void setDate(LocalDate date, LocalTime time) {
        this.time = time.atDate(date);
    }

    public void setLocation(String location, IncidentLocation.LocationType type) {
        this.location = new IncidentLocation(location, type, null, null);
    }

    public void addAgency(Agency agency) {
        this.agencies.add(agency);
    }

    public void removeAgency(Agency agency) {
        if (!this.agencies.remove(agency)) {
            throw new IllegalArgumentException("Agency '" + agency + "' is not in the incident '" + this + "'");
        }
    }

    public void addContributor(String contributor) {
        if (this.contributors.contains(contributor)) {
            return;
        }
        this.contributors.add(contributor);
    }

    public List<String> formatNarrative() {
        if (this.narrative == null || this.narrative.isEmpty()) {
            return null;
        }

        List<String> response = new ArrayList<>();
        for (IncidentNarrativeEntry entry : this.narrative) {
            response.add("<t:" + entry.getTime().toEpochSecond(OffsetDateTime.now().getOffset()) + ":t>"
                    + " `" + entry.getType().name() + "` " +
                    entry.getEntry()
            );
        }
        return response;
    }

    public void post() {
        Main.receiveChannels.forEach(tc -> tc.sendMessage(this.format()).queue());
    }

    public MessageCreateData format() {
        long unix = this.time.toEpochSecond(OffsetDateTime.now().getOffset());
        List<String> narrative = this.formatNarrative();
        return MessageCreateData.fromContent(String.format(
                """
                        # :rotating_light: %s :rotating_light:
                        [<t:%d:d>@<t:%d:T> // <t:%d:R>]
                        
                        **Responding:** %s
                        **Location:** %s
                        
                        **Narrative:**
                        %s
                        """,
                this.type.getCompleteName(),
                unix, unix, unix,
                String.join(", ", this.agencies.stream().map(Agency::getAgencyFormatted).toList()),
                this.location.getData(),
                narrative != null ? String.join(", ", this.formatNarrative()) : "None"
        ));
    }

    public MessageCreateData formatAdmin() {
        long unix = this.time.toEpochSecond(OffsetDateTime.now().getOffset());
        List<String> narrative = this.formatNarrative();

        StringJoiner respondingAgencies = new StringJoiner("\n");
        int index = 0;
        for (Agency agency : this.agencies) {
            respondingAgencies.add("- **" + agency.getAgencyLong().toUpperCase() + "**");
            respondingAgencies.add((index == 0 ? "  " : "") + "  - " +
                    "(shorthand `" + agency.getAgencyShorthand() + "`, formatted `" + agency.getAgencyFormatted() + "`, emoji :" + agency.getAgencyShorthand() + ":)"
                    );
            index++;
        }
        return MessageCreateData.fromEmbeds(new EmbedBuilder()
                        .setTitle("ADMIN OVERVIEW")
                        .setDescription("Incident #`" + this.id + "`\n"
                                + "Message Ids (" + this.messageIds.size() + "): " + String.join(", ", this.messageIds.stream().map(String::valueOf).toList())
                        )
                        .setFooter("Contributors: " + String.join(", ", this.contributors))
                        .addField("Call Type",
                                this.type.getCompleteName() + "\n\n" +
                                        "(base `" + this.type.getType() + "`, tag `" + this.type.getTag().name + "`, qualifier #`" + this.type.getQualifierChoice() + "`)",
                                true
                        )
                        .addField("Date/Time",
                                "Date: <t:" + unix + ":d>\n" +
                                        "Time: <t:" + unix + ":T>\n" +
                                        "Relative: <t:" + unix + ":R>\n" +
                                        "Unix: `" + unix + "`", true
                                )
                        .addField("Location",
                                "Type: `" + this.location.getType().name() + "`\n" +
                                        "Data: `" + this.location.getData() + "`\n" +
                                        "Common Name: `" + ifNull(this.location.getCommonName()) + "`\n" +
                                        "Venue: `" + ifNull(this.location.getVenue()) + "`",
                                true
                                )
                        .addField("Responding Agencies",
                                respondingAgencies.toString(), false
                                )
                        .addField("Narrative",
                                narrative != null ? String.join("\n", narrative) : "None",
                                false
                                )
                        .setColor(new Color(255, 94, 94))
                .build());
    }

    private String ifNull(@Nullable String text) {
        if (text == null) {
            return " ";
        }
        return text;
    }

}