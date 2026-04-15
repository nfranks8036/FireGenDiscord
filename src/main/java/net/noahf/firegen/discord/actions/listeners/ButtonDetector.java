package net.noahf.firegen.discord.actions.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

public class ButtonDetector extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();
        User user = event.getUser();

        if (!id.startsWith("firegen-")) {
            return;
        }

        Log.info(user.getName() + " (" + user.getIdLong() + ") pressed button '" + id + "'");

        try {
            Main.actions.processAction(event, id);
        } catch (Exception exception) {
            DiscordMessages.error(event, "An error occurred processing your button press", exception);
        }
    }
}
