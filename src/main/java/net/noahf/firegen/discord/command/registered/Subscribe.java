package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.utilities.DiscordMessages;

public class Subscribe extends Command {

    public Subscribe() {
        super("subscribe", "Subscribes a channel to listen for radio-activity events in the NRV.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.CHANNEL, "channel",
                                        "The channel to subscribe. Defaults to the current one if not set.",
                                        false, false
                                ),
                        })
                        .finish());
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        DiscordMessages.selfDestruct(event, 5,
                "This does not work yet. Stop trying. Thanks :)\n\\- Management"
        );
    }
}
