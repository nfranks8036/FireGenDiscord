package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;

public class CallTypes extends Command {

    public CallTypes() {
        super("call-types", "The call types that are available in the FireGen system.", CommandFlags.none());
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.reply("Succeed.").queue();
    }
}
