package net.noahf.firegen.discord.utilities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class ErrorEmbed {

    public static MessageEmbed error(String message) {
        return new EmbedBuilder()
                .setColor(new Color(121, 0, 0))
                .setTitle("An error occurred")
                .setDescription(message)
                .setFooter("Try again later or with different parameters.")
                .build();
    }

}
