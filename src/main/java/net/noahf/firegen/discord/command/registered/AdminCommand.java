package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;

public class AdminCommand extends Command {

    public AdminCommand() {
        super(
                "admin-cmd", "Execute an admin command.",
                CommandFlags.include()
                        .permissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "command", "The command to execute", true, false),
                                new OptionData(OptionType.STRING, "parameter", "The parameter", false, false)
                        })
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        OptionMapping commandMapping = event.getOption("command");
        if (commandMapping == null) {
            DiscordMessages.error(event, "Expected command."); return;
        }

        String command = commandMapping.getAsString();

        OptionMapping parameterMapping = event.getOption("parameter");
        String parameter = parameterMapping != null ? parameterMapping.getAsString() : null;

        switch (command) {
            case "force-post" -> {
                if (parameter == null) {
                    DiscordMessages.error(event, "Expected parameter for 'force-post'.");
                    return;
                }

                IncidentImpl incident = Main.incidents.getIncidentBy(Long.parseLong(parameter));
                if (incident == null) {
                    DiscordMessages.error(event, "Invalid parameter.");
                    return;
                }

                incident.admin_wipeMessages();
                incident.postUpdate();
            }
            default -> {
                DiscordMessages.error(event, "Invalid command.");
            }
        }
    }
}
