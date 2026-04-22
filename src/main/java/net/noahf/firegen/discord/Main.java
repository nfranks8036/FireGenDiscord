package net.noahf.firegen.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.actions.ActionsManager;
import net.noahf.firegen.discord.actions.listeners.ButtonDetector;
import net.noahf.firegen.discord.actions.listeners.ModalDetector;
import net.noahf.firegen.discord.actions.listeners.StringSelectDetector;
import net.noahf.firegen.discord.command.CommandManager;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.utilities.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String TOKEN = null;
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static JDA JDA;
    public static CommandManager commands;
    public static IncidentManager incidents;
    public static ActionsManager actions;

    public static List<TextChannel> adminChannels = new ArrayList<>();
    public static List<TextChannel> receiveChannels = new ArrayList<>();

    private static void loadChannels(JDA jda) {
        adminChannels.add(jda.getTextChannelById(1492362581623439581L)); // BFD Tracker - bot
        adminChannels.add(jda.getTextChannelById(1493112158257549462L)); // BFD Tracker - test-admin

        receiveChannels.add(jda.getTextChannelById(1492362595439611925L)); // BFD Tracker - bot-output
        receiveChannels.add(jda.getTextChannelById(1493112167258525768L)); // BFD Tracker - test-radio
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        try {
            if (TOKEN == null) TOKEN = getExternalInfo("TOKEN");
        } catch (Exception exception) {
            Log.error("Failed to find token from environment: " + exception, exception);
        }

        Log.info("Checking for token...");
        if (TOKEN == null)
            throw new RuntimeException("Cannot find token value (token = null)");

        Log.info("Building JDA...");
        JDA = JDABuilder.createDefault(TOKEN)
                .setActivity(Activity.customStatus("Listening to the radio"))
                .setStatus(OnlineStatus.ONLINE)
                .disableCache(
                        CacheFlag.SCHEDULED_EVENTS, CacheFlag.VOICE_STATE
                )
                .setEnabledIntents(
                        GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_EXPRESSIONS
                )
                .addEventListeners(new ButtonDetector(), new ModalDetector(), new StringSelectDetector())
                .build()
                .awaitReady();

        loadChannels(JDA);

        FireGenVariables vars = new FireGenVariables();
        vars.resetToDefault();

        incidents = new IncidentManager(vars);
        actions = new ActionsManager();
        commands = new CommandManager();

        Log.info("Started in " + (System.currentTimeMillis() - start) + "ms!");
    }

    private static String getExternalInfo(String key) {
        return (System.getenv().getOrDefault(key, System.getProperty(key)));
    }

}
