package net.noahf.firegen.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.noahf.firegen.discord.command.CommandManager;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.listeners.ButtonDetector;
import net.noahf.firegen.discord.utilities.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.basic.BasicButtonListener;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String TOKEN = null;
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static JDA JDA;
    public static CommandManager commands;
    public static IncidentManager incidents;

    public static boolean maintenance = false;
    public static List<Long> allowedDuringMaintenance = new ArrayList<>(
            List.of(351410272256262145L, 771098554600915004L)
    );

    public static List<TextChannel> adminChannels = new ArrayList<>();
    public static List<TextChannel> receiveChannels = new ArrayList<>();

    private static void loadChannels(JDA jda) {
        adminChannels.add(jda.getTextChannelById(1491906498974978290L)); // BFD Tracker - bot
        adminChannels.add(jda.getTextChannelById(725503983678128199L)); // Personal - 0

        receiveChannels.add(jda.getTextChannelById(1491906756731863103L)); // Personal - radio-activity
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

        String isMaintenance = getExternalInfo("maintenance");
        if (isMaintenance != null) {
            Log.info("-".repeat(50));
            Log.info("Environment variable 'maintenance' set to '" + isMaintenance + "'...");
            Log.info("-".repeat(50));
            Main.maintenance = isMaintenance.equalsIgnoreCase("true");
        }

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
                .addEventListeners(new ButtonDetector())
                .build()
                .awaitReady();

        if (maintenance) {
            Presence presence = JDA.getPresence();
            presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
            presence.setActivity(Activity.customStatus("Down for Maintenance"));
        }

        loadChannels(JDA);

        commands = new CommandManager();
        incidents = new IncidentManager();

        Log.info("Started in " + (System.currentTimeMillis() - start) + "ms!");
    }

    private static String getExternalInfo(String key) {
        return (System.getenv().getOrDefault(key, System.getProperty(key)));
    }

}
