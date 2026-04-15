package net.noahf.firegen.discord.actions.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

public class ModalDetector extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String id = event.getModalId();
        User user = event.getUser();

        if (!id.startsWith("firegen-")) {
            return;
        }

        Log.info(user.getName() + " (" + user.getIdLong() + ") interacted with modal '" + id + "'");

        try {
            Main.actions.processAction(event, id);
        } catch (Exception exception) {
            DiscordMessages.error(event, "An error occurred processing your modal submission", exception);
        }
    }
}
