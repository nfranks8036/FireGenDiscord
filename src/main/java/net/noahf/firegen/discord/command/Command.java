package net.noahf.firegen.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents an abstract Command. This is the class that all commands should extend to provide proper functionality.
 * To be autoregistered, the command must be located in the {@link CommandManager#COMMANDS_PACKAGE commands package}.
 */
public abstract class Command {

    final String name;
    final String description;
    final CommandFlags flags;

    final List<CommandData> data;

    /**
     * Creates a new command from the available data with no {@link CommandFlags flags}.
     * @param name the name of the command, typically the main command name
     * @param description the description of the command that will show up in the command context menu
     */
    public Command(@Nullable String name, @Nullable String description) {
        this(name, description, CommandFlags.none());
    }

    /**
     * Creates a new command from the available data.
     * @param name the name of the command, typically the main command name
     * @param description the description of the command that will show up in the command menu on Discord
     * @param flags the {@link CommandFlags} class, can be built with the {@link CommandFlags#include()} method
     */
    public Command(@Nullable String name, @Nullable String description, CommandFlags flags) {
        this.name = name;
        this.description = description;
        this.flags = flags;
        this.data = new ArrayList<>();

        this.addCommand(name);
        if (this.flags.aliases != null) {
            for (String alias : this.flags.aliases) {
                this.addCommand(alias);
            }
        }
    }

    /**
     * Adds the command with a given name to the {@link CommandData} list by instantiating a new {@link SlashCommandData}
     * with the appropriate options.
     * @param name the name of the command, can be the alias or the actual name
     */
    void addCommand(String name) {
        // we use the 'name' parameter because this is also used for building 'alias' command data
        SlashCommandData data = Commands.slash(name, this.description);

        if (this.flags.options != null && Arrays.stream(this.flags.options).noneMatch(Objects::isNull)) {
            // options are essentially arguments to a command
            data = data.addOptions(this.flags.options);
        }

        if (this.flags.permissions != null) {
            data = data.setDefaultPermissions(this.flags.permissions);
        }

        this.data.add(data);
    }

    public abstract void command(SlashCommandInteractionEvent event);
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) { throw new UnsupportedOperationException(); }

}
