package net.noahf.firegen.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.noahf.firegen.discord.utilities.Log;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String TOKEN = null;
    public static JDA JDA;

    public static boolean maintenance = false;
    public static List<Long> allowedDuringMaintenance = new ArrayList<>(
            List.of(351410272256262145L, 771098554600915004L)
    );

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        try {
            if (TOKEN == null) TOKEN = getExternalInfo("TOKEN");
        } catch (Exception exception) {
            Log.error("Failed to find token from environment: " + exception, exception);
        }

        Log.text("Checking for token...");
        if (TOKEN == null)
            throw new RuntimeException("Cannot find token value (token = null)");

        String isMaintenance = getExternalInfo("maintenance");
        if (isMaintenance != null) {
            Log.text("-".repeat(50));
            Log.text("Environment variable 'maintenance' set to '" + isMaintenance + "'...");
            Log.text("-".repeat(50));
            Main.maintenance = isMaintenance.equalsIgnoreCase("true");
        }

        Log.text("Building JDA...");
        JDA = JDABuilder.createDefault(TOKEN)
                .setActivity(Activity.listening("to the radio"))
                .setStatus(OnlineStatus.ONLINE)
                .setEnabledIntents(
                        GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS
                )
                .build()
                .awaitReady();

        if (maintenance) {
            Presence presence = JDA.getPresence();
            presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
            presence.setActivity(Activity.customStatus("Down for Maintenance"));
        }
    }

    private static String getExternalInfo(String key) {
        return (System.getenv().getOrDefault(key, System.getProperty(key)));
    }

}
