package net.noahf.firegen.discord.utilities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DiscordMessages {

    public static void error(IReplyCallback event, String message, @Nullable Exception cause) {
        EmbedBuilder responseBuilder = new EmbedBuilder()
                .setColor(new Color(121, 0, 0))
                .setTitle("An error occurred")
                .setDescription(message)
                .setFooter("Try again later or with different parameters.");

        if (cause != null) {
            responseBuilder = responseBuilder
                    .addField("Caused by:", cause.toString(), false);
            Log.error(event.getUser().getName() + " caused: " + message, cause);
        }

        if (event.isAcknowledged()) {
            // if this interaction has already been acknowledged, then we can't reply again so we must instead edit
            event.getHook().editOriginalEmbeds(responseBuilder.build()).queue();
        } else {
            // if the interaction has never been acknowledged, we can safely send a reply embed
            event.replyEmbeds(responseBuilder.build()).setEphemeral(true).queue();
        }
    }

    public static void error(IReplyCallback event, String message) {
        DiscordMessages.error(event, message, null);
    }

    public static void selfDestruct(IReplyCallback event, int selfDestructAfter, String message) {
        long destruct = Time.getUnixOffset(selfDestructAfter + 1, TimeUnit.SECONDS);
        message = message +
                "\n\n-# This message will self-destruct <t:" + destruct + ":R>";

        if (event.isAcknowledged()) {
            // if this interaction has already been acknowledged, then we can't reply again so we must instead edit
            event.getHook().editOriginal(message).setComponents(new ArrayList<>()).complete().delete().queueAfter(selfDestructAfter, TimeUnit.SECONDS);
        } else {
            // if the interaction has never been acknowledged, we can safely send a reply embed
            event.reply(message).setEphemeral(true).complete().deleteOriginal().queueAfter(selfDestructAfter, TimeUnit.SECONDS);
        }
    }

    public static void selfDestructEdit(IMessageEditCallback event, int selfDestructAfter, String message) {
        long destruct = Time.getUnixOffset(selfDestructAfter + 1, TimeUnit.SECONDS);
        message = message +
                "\n\n-# This message will self-destruct <t:" + destruct + ":R>";

        event.editMessage(message)
                .setComponents(new ArrayList<>())
                .complete()
                .deleteOriginal()
                .queueAfter(selfDestructAfter, TimeUnit.SECONDS);
    }

    public static void noMessage(IReplyCallback event) {
        if (event.isAcknowledged()) {
            event.getHook().deleteOriginal().queue();
        } else {
            event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();
        }
    }

    public static void noMessageEdit(IMessageEditCallback event) {
        event.deferEdit().complete().deleteOriginal().queue();
    }

    @Deprecated
    public static MessageEmbed error(String message) {
        return new EmbedBuilder()
                .setColor(new Color(121, 0, 0))
                .setTitle("An error occurred")
                .setDescription(message)
                .setFooter("Try again later or with different parameters.")
                .build();
    }

}
