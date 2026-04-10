package net.noahf.firegen.discord.incidents.structure;

import lombok.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocation;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@RequiredArgsConstructor
public class Incident {

    private final IncidentManager manager;
    @Getter
    private final long id;

    private @Getter @NotNull IncidentType type;
    private @Getter final List<Agency> agencies;
    private @Getter @Setter @NotNull IncidentLocation location;
    private @Getter @NotNull LocalDateTime time;

    private final @Getter List<IncidentNarrativeEntry> narrative;

    private final List<Message> receivingMessages = new ArrayList<>();
    private final List<Message> adminMessages = new ArrayList<>();

    private final List<String> contributors = new ArrayList<>();

    public void setType(String type) {
        IncidentType newType = manager.getTypeFromString(type);
        if (newType == null) {
            throw new IllegalArgumentException("Expected a valid incident type from file, got '" + type + "'");
        }

        this.type = newType;
    }

    public long getUnix() {
        return this.time.toEpochSecond(OffsetDateTime.now().getOffset());
    }

    public void setTime(LocalTime time) {
        this.setDate(LocalDate.now(), time);
    }

    public void setDate(LocalDate date, LocalTime time) {
        this.time = time.atDate(date);
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


    public String getFormattedId() {
        return this.time.format(DateTimeFormatter.ofPattern("yyyy")) + "-" + this.getId();
    }

    public void postUpdate() {
        if (this.receivingMessages.isEmpty()) {
            String startingMessage = "New Call- " + this.type.getCompleteName();
            if (this.location.isSet() && !this.location.format().isBlank()) {
                startingMessage = startingMessage + "\nWhere- " + this.location.format();
            }
            if (!this.agencies.isEmpty()) {
                startingMessage = startingMessage + "\nWho- " + String.join(", ", this.agencies.stream().map(Agency::getAgencyShorthand).toList());
            }
            startingMessage = startingMessage + "\nWhen- <t:" + getUnix() + ":t>";

            for (TextChannel channel : Main.receiveChannels) {
                Log.info("Sending starting message in #" + channel.getName() + " in " + channel.getGuild().getName() + "...");
                this.receivingMessages.add(channel.sendMessage(startingMessage).complete());
            }

            for (TextChannel channel : Main.adminChannels) {
                this.adminMessages.add(channel.sendMessage("New incident " + this.type.getCompleteName() + " created by " + this.contributors.get(0))
                                .setComponents(
                                        ActionRow.of(
                                                net.dv8tion.jda.api.components.buttons.Button.primary("firegen-" + this.getId() + "-incidenttype", "Edit Type"),
                                                net.dv8tion.jda.api.components.buttons.Button.primary("firegen-" + this.getId() + "-datetime", "Edit Date/Time"),
                                                net.dv8tion.jda.api.components.buttons.Button.primary("firegen-" + this.getId() + "-location", "Edit Location")
                                        ),
                                        ActionRow.of(
                                                net.dv8tion.jda.api.components.buttons.Button.secondary("firegen-disabled-narrative", "Narrative:").asDisabled(),
                                                net.dv8tion.jda.api.components.buttons.Button.success("firegen-" + this.getId() + "-addnarrative", "Add"),
                                                net.dv8tion.jda.api.components.buttons.Button.danger("firegen-" + this.getId() + "-removenarrative", "Rem"),
                                                net.dv8tion.jda.api.components.buttons.Button.secondary("firegen-" + this.getId() + "-editnarrative", "Edit")
                                        ),
                                        ActionRow.of(
                                                net.dv8tion.jda.api.components.buttons.Button.secondary("firegen-disabled-agencies", "Agencies:").asDisabled(),
                                                net.dv8tion.jda.api.components.buttons.Button.success("firegen-" + this.getId() + "-addagency", "Add"),
                                                Button.danger("firegen-" + this.getId() + "-removeagency", "Rem")
                                        )
                                )
                        .complete());
            }
        }

        String fullMessage = this.formatReceiving();
        for (Message message : this.receivingMessages) {
            Log.info("Updating incident " + this.getFormattedId() + " (" + this.getType().getCompleteName()
                    + ") message in #" + message.getChannel().getName() + " in " + message.getGuild().getName() + "...");
            message.editMessage(fullMessage).queue();
        }

        MessageEmbed adminMsg = this.formatAdmin();
        for (Message message : this.adminMessages) {
            message.editMessageEmbeds(adminMsg).queue();
        }
    }

    public String formatReceiving() {
        List<String> narrative = this.formatNarrative();
        return String.format(
                """
                        # %s
                        [<t:%d:d>@<t:%d:T> // <t:%d:R>]
                        
                        **Responding:** %s
                        **Location:** %s
                        """ +
                        (narrative != null ? "\n\n**Narrative:**\n%s" : "")
                        + "\n-# Incident " + this.getFormattedId(),
                this.type.getCompleteName(),
                getUnix(), getUnix(), getUnix(),
                String.join(", ", this.agencies.stream().map(Agency::getAgencyFormatted).toList()),
                this.location.format(),
                narrative != null ? String.join(", ", this.formatNarrative()) : "None"
        );
    }

    public MessageEmbed formatAdmin() {
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
        return new EmbedBuilder()
                        .setTitle("ADMIN OVERVIEW")
                        .setDescription("Incident `" + this.getFormattedId() + "`\n"
                                + "Message Ids (" + this.receivingMessages.size() + "): " + String.join(", ", this.receivingMessages.stream().map(String::valueOf).toList())
                        )
                        .setFooter("Contributors: " + String.join(", ", this.contributors))
                        .addField("Call Type",
                                this.type.getCompleteName() + "\n\n" +
                                        "(base `" + this.type.getType() + "`, tag `" + this.type.getTag().name + "`, qualifier #`" + this.type.getQualifierChoice() + "`)",
                                true
                        )
                        .addField("Date/Time",
                                "Date: <t:" + getUnix() + ":d>\n" +
                                        "Time: <t:" + getUnix() + ":T>\n" +
                                        "Relative: <t:" + getUnix() + ":R>\n" +
                                        "Unix: `" + getUnix() + "`", true
                                )
                        .addField("Location",
                                "Type: `" + this.location.getType().name() + "`\n" +
                                        "Data: " + String.join(", ", this.location.getData().stream().map(s -> "`" + s + "`").toList()) + "\n" +
                                        "Common Name: `" + (this.location.getCommonName() != null ? this.location.getCommonName() : " ") + "`\n" +
                                        "Venue: `" + (this.location.getVenue() != null ? this.location.getVenue() : " ") + "`\n" +
                                        "Formatted: `" + this.location.format() + "`"
                                ,
                                false
                                )
                        .addField("Responding Agencies",
                                respondingAgencies.toString().isBlank() ? "None" : respondingAgencies.toString(), false
                                )
                        .addField("Narrative",
                                narrative != null ? String.join("\n", narrative) : "None",
                                false
                                )
                        .setColor(new Color(255, 94, 94))
                .build();
    }

}