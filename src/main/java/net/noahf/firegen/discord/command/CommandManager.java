package net.noahf.firegen.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.Log;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String commandString = event.getCommandString();
        try {
            Log.text(user.getName() + " (" + user.getIdLong() + "): " + commandString);
            if (Main.maintenance && !Main.allowedDuringMaintenance.contains(user.getIdLong())) {
                // send maintenance status
                return;
            }
        }
    }
}
