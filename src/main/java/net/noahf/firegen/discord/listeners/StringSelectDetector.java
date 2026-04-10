package net.noahf.firegen.discord.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

public class StringSelectDetector extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String id = event.getComponentId();
        User user = event.getUser();

        if (!id.startsWith("firegen-")) {
            return;
        }

        Log.info(user.getName() + " (" + user.getIdLong() + ") interacted with string selector '" + id + "'");

        String incident = id.split("-")[1];
        String command = id.split("-")[2];

        Main.incidents.processAction(event, incident, command);
    }
}
