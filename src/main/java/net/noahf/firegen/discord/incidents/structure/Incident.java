package net.noahf.firegen.discord.incidents.structure;

import lombok.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.Components;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocation;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

@RequiredArgsConstructor
public class Incident {

    private final IncidentManager manager;
    @Getter private final long id;

    private @Getter @Setter IncidentStatus status;

    private @Getter @NotNull IncidentType type;
    private @Getter @NotNull List<Agency> agencies;
    private @Getter @Setter @NotNull IncidentLocation location;
    private @Getter @NotNull LocalDateTime time;

    private final @Getter List<IncidentNarrativeEntry> narrative;

    private final List<Message> receivingMessages;
    private final List<Message> adminMessages;

    private final List<String> contributors;

    private final List<MessageTopLevelComponent> adminComponents;

    public Incident(IncidentManager manager) {
        this.manager = manager;
        this.id = new Random(System.currentTimeMillis()).nextLong(1000000, 9999999);
        this.status = IncidentStatus.PENDING;
        this.type = this.manager.getNewIncidentType();
        this.location = new IncidentLocation();
        this.time = LocalDateTime.now();

        this.agencies = new ArrayList<>();
        this.narrative = new ArrayList<>();
        this.receivingMessages = new ArrayList<>();
        this.adminMessages = new ArrayList<>();
        this.contributors = new ArrayList<>();

        this.adminComponents = new ArrayList<>(List.of(
                // components following are the button row that are used in the admin channel
                // id should be in the format of 'firegen-<incident ID>-<command>-<additional info>'
                ActionRow.of(
                        Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                        Button.danger("firegen-" + this.getId() + "-status", "Close Incident")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-incident1", "Edit:").asDisabled(),
                        Button.primary("firegen-" + this.getId() + "-incidenttype", "Type"),
                        Button.primary("firegen-" + this.getId() + "-datetime", "Date/Time")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-incident2", "Edit:").asDisabled(),
                        Button.primary("firegen-" + this.getId() + "-location", "Location"),
                        Button.primary("firegen-" + this.getId() + "-agencies", "Agencies")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-narrative", "Narrative:").asDisabled(),
                        Button.success("firegen-" + this.getId() + "-addnarrative", "Add"),
                        Button.danger("firegen-" + this.getId() + "-hidenarrative", "Hide")
                )
        ));
    }

    public void setType(String type) {
        IncidentType newType          = manager.getTypeFromString(type);
        if (type.startsWith("custom:")) {
            type = type.substring("custom:".length()).toUpperCase();
            newType = new IncidentType(type, IncidentTypeTag.DEFAULT, 0);
        }

        if (newType == null) {
            throw new IllegalArgumentException("Expected a valid incident type from file, got '" + type + "'");
        }

        this.type = newType;
    }

    public long getUnix() {
        return Time.getUnix(this.time);
    }

    public void setTime(LocalTime time) {
        this.setDate(LocalDate.now(), time);
    }

    public void setDate(LocalDate date, LocalTime time) {
        this.time = time.atDate(date);
    }

    public void addContributor(String contributor) {
        if (this.contributors.contains(contributor)) {
            return;
        }
        this.contributors.add(contributor);
    }

    public void addNarrative(User user, IncidentNarrativeEntry.EntryType type, String narrative) {
        this.narrative.add(new IncidentNarrativeEntry(LocalDateTime.now(), user.getIdLong(), narrative, type));
    }

    public List<IncidentNarrativeEntry> getOutputNarrative() {
        return this.narrative.stream().filter(ine -> ine.getType() == IncidentNarrativeEntry.EntryType.NARRATIVE).toList();
    }

    public void injectNarrative(IncidentNarrativeEntry entry) {
        for (int i = 0; i < this.narrative.size(); i++) {
            IncidentNarrativeEntry element = this.narrative.get(i);
            if (element.getId() != entry.getId()) {
                continue;
            }
            this.narrative.set(i, entry);
            return;
        }
        throw new IllegalStateException("Narrative with ID '" + entry.getId() + "' does not exist in the incident with ID '" + this.getFormattedId() + "'");
    }

    public void setAgencies(List<Agency> agencies) {
        if (agencies.isEmpty()) {
            this.status =  IncidentStatus.PENDING;
        } else {
            this.status =  IncidentStatus.ACTIVE;
        }
        this.agencies = agencies;
    }

    public @NotNull List<String> formatNarrative(boolean admin) {
        if (this.narrative == null || this.narrative.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> response = new ArrayList<>();
        for (IncidentNarrativeEntry entry : this.narrative) {
            if (!admin && entry.getType() != IncidentNarrativeEntry.EntryType.NARRATIVE) {
                // we don't want admin update logs to be included in the narrative for the public necessarily
                continue;
            }
            response.add(admin ? entry.formatAdmin() : entry.formatReceiver());
        }
        return response;
    }


    public String getFormattedId() {
        return this.time.format(DateTimeFormatter.ofPattern("yyyy")) + "-" + this.getId();
    }

    /**
     * Post the incident changes to the saved messages. Used for updating subscribed servers with new information. <br>
     * <b>This method will block the main thread IF the incident has never been posted before.</b> <br>
     * This is required to ensure the order of the initial message and then edit message when the incident is created.
     */
    public void postUpdate() {
        if (this.receivingMessages.isEmpty()) {
            // if condition is met:
            // this incident has never been posted in any channel yet, so it's likely a new one.

            String startingMessage = "New Call- " + this.type.getCompleteName();
            if (this.location.isSet() && !this.location.format().isBlank()) {
                startingMessage = startingMessage + "\nWhere- " + this.location.format();
            }
            if (!this.agencies.isEmpty()) {
                startingMessage = startingMessage + "\nWho- " + String.join(", ", this.agencies.stream().map(Agency::getShorthand).toList());
            }
            startingMessage = startingMessage + "\nWhen- <t:" + getUnix() + ":t>";

            // send a starting message to the subscribed channels, this will be quickly changed by the following edit
            for (TextChannel channel : Main.receiveChannels) {
                Log.info("Sending starting message in #" + channel.getName() + " in " + channel.getGuild().getName() + "...");
                this.receivingMessages.add(channel.sendMessage(startingMessage).complete());
            }

            // send a starting message to the admin channels, this will be quickly changed by the following edit THOUGH
            // the content will remain
            for (TextChannel channel : Main.adminChannels) {
                this.adminMessages
                        .add(channel.sendMessage("New incident " + this.type.getCompleteName() + " created by " + this.contributors.get(0))
                        .setComponents(this.adminComponents)
                        .complete());
            }
        }

        // edit the messages with the updated detailed content
        String fullMessage = this.formatReceiving();
        for (Message message : this.receivingMessages) {
            Log.info("Updating incident " + this.getFormattedId() + " (" + this.getType().getCompleteName()
                    + ") message in #" + message.getChannel().getName() + " in " + message.getGuild().getName() + "...");
            message.editMessage(fullMessage).queue();
        }

        // edit the admin messages with an updated admin panel
        MessageEmbed adminMsg = this.formatAdmin();
        for (Message message : this.adminMessages) {

            // add or remove the components if the status requires it
            if (this.status.isActive() && message.getComponents().size() <= this.adminComponents.size()) {
                message.editMessageComponents(this.adminComponents).queue();
            } else if (!this.status.isActive() && message.getComponents().size() > 1) {
                message.editMessageComponents(ActionRow.of(
                        Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                        Button.success("firegen-" + this.getId() + "-status", "Re-open Incident")
                )).queue();
            }

            message.editMessageEmbeds(adminMsg).queue();
        }
    }

    public String formatReceiving() {
        List<String> narrative = this.formatNarrative(false);
        return String.format(
                """
                        # %s %s
                        [<t:%d:d>@<t:%d:t> // <t:%d:R>]
                        
                        **Responding:** %s
                        **Location:** %s""" +
                        (!narrative.isEmpty() ? "\n\n**Narrative:**\n%s" : ""),
                this.type.getCompleteName(),
                this.status.getEmoji(),
                getUnix(), getUnix(), getUnix(),
                String.join(", ", this.agencies.stream().map(Agency::getFormatted).toList()),
                this.location.format(),
                !narrative.isEmpty() ? String.join("\n", narrative) : "None"
        );
    }

    public MessageEmbed formatAdmin() {
        List<String> narrative = this.formatNarrative(true);

        StringJoiner respondingAgencies = new StringJoiner("\n");
        int index = 0;
        for (Agency agency : this.agencies) {
            respondingAgencies.add("- **" + agency.getLonghand().toUpperCase() + "**");
            respondingAgencies.add((index == 0 ? "  " : "") + "  - " +
                    "(shorthand `" + agency.getShorthand() + "`, formatted `" + agency.getFormatted() + "`, emoji " + agency.getEmoji() + ")"
                    );
            index++;
        }
        return new EmbedBuilder()
                        .setTitle("ADMIN OVERVIEW")
                        .setDescription("Incident `" + this.getFormattedId() + "`"
                                + "\nStatus: " + this.status.getEmoji() + " " + this.status.name()
                                + "\nMessages (" + this.receivingMessages.size() + "): " + String.join(" , ", this.receivingMessages.stream().map(msg ->
                                    "https://discord.com/channels/" + msg.getGuild().getId() + "/" + msg.getChannel().getId() + "/" + msg.getId()
                                ).toList())
                        )
                        .setFooter("Contributors: " + String.join(", ", this.contributors))
                        .addField("Call Type",
                                this.type.getCompleteName() + "\n\n" +
                                        "(base `" + this.type.getType() + "`, " +
                                        "tag `" + this.type.getTag().name + "`, " +
                                        "qualifier `" + (this.type.getTag().getQualifier() != null ? this.type.getTag().getQualifier().getQualifiers().get(this.type.getQualifierChoice()) : "None") + "`)",
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
                                !narrative.isEmpty() ? String.join("\n", narrative) : "None",
                                false
                                )
                        .setColor(new Color(255, 94, 94))
                .build();
    }

}