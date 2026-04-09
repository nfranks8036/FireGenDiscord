package net.noahf.firegen.discord.listeners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

public class ButtonDetector extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("firegen-")) {
            return;
        }

        String incident = id.split("-")[1];
        String command = id.split("-")[2];


    }
}
