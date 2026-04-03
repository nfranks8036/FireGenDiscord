package net.noahf.firegen.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.Log;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class Command {






    final String name;
    final String description;
    final CommandFlags flags;

    final List<CommandData> data;

    public Command(@Nullable String name, @Nullable String description) {
        this(name, description, CommandFlags.none());
    }

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

    void addCommand(String name) {
        // we use the 'name' parameter because this is also used for building 'alias' command data
        SlashCommandData data = Commands.slash(name, this.description);
        if (this.flags.options != null && Arrays.stream(this.flags.options).noneMatch(Objects::isNull)) {
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
