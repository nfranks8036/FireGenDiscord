package net.noahf.firegen.discord.incidents.buttonaction;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.ModalAction;
import net.noahf.firegen.discord.incidents.structure.Incident;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class EditDateTime implements ButtonAction, ModalAction {

    @Override
    public String getName() {
        return "datetime";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {
        TextInput time = TextInput.create("time", TextInputStyle.SHORT)
                .setPlaceholder("HH:mm:ss")
                .setMinLength(6)
                .setMaxLength(10)
                .setRequired(true)
                .setValue(incident.getTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .build();

        TextInput date = TextInput.create("date", TextInputStyle.SHORT)
                .setPlaceholder("MM/dd/yyyy")
                .setMinLength(6)
                .setMaxLength(16)
                .setRequired(false)
                .setValue(incident.getTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))
                .build();

        Modal modal = Modal.create("firegen-" + incident.getId() + "-datetime", "Date/Time of " + incident.getFormattedId())
                .addComponents(Label.of("Time of Incident", "In the format of (HH:mm:ss)", time), Label.of("Date of Incident", "In the format of (MM/dd/yyyy)", date))
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void execute(Incident incident, ModalInteractionEvent event) {
        ModalMapping timeMapping = event.getValue("time");
        if (timeMapping == null) {
            event.replyEmbeds(DiscordMessages.error("Expected value 'time' to be set in modal, found none.")).setEphemeral(true).queue();
            return;
        }
        LocalTime time = LocalTime.parse(timeMapping.getAsString(), DateTimeFormatter.ofPattern("HH:mm:ss"));

        ModalMapping dateMapping = event.getValue("date");
        LocalDate date = incident.getTime().toLocalDate();
        if (dateMapping != null) {
            date = LocalDate.parse(dateMapping.getAsString(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }

        if (date.isAfter(ChronoLocalDate.from(LocalDateTime.now(OffsetDateTime.now().getOffset())))) {
            event.replyEmbeds(DiscordMessages.error("Cannot set the date and time to a future date!")).setEphemeral(true).queue();
            return;
        }

        long unix = Time.getUnix(time.atDate(date));
        long destruct = Time.getUnixOffset(6, TimeUnit.SECONDS);
        event.reply("The date and time for this incident was updated to <t:" + unix + ":d> @ <t:" + unix + ":T>, which was <t:" + unix + ":R>" +
                "\n\n-# This message will self-destruct <t:" + destruct + ":R>"
        ).setEphemeral(true).complete().deleteOriginal().queueAfter(5, TimeUnit.SECONDS);
        incident.addContributor(event.getUser().getName());

        incident.setDate(date, time);
        incident.postUpdate();
    }




    @Override
    public void execute(Incident incident, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(incident, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(incident, e); }
    }
}
