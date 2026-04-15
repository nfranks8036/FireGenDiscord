package net.noahf.firegen.discord.actions.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.DiscordMessages;
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

        try {
            Main.actions.processAction(event, id);
        } catch (Exception exception) {
            DiscordMessages.error(event, "An error occurred processing your string selection", exception);
        }
    }
}
