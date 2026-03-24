package net.noahf.firegen.discord.command;

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

    private static final String COMMANDS_PACKAGE = "net.noahf.firegen.discord.commands";

    private static final List<Command> commands = new ArrayList<>();

    static void register() {
        // find command classes and instantiate
        for (Class<? extends Command> clazz : new Reflections(COMMANDS_PACKAGE).getSubTypesOf(Command.class)) {
            try {
                @Nullable Constructor<?> constructor =
                        Arrays.stream(clazz.getDeclaredConstructors())
                                .filter(c -> c.getParameterCount() == 0)
                                .findFirst().orElse(null);
                if (constructor == null) {
                    throw new IllegalArgumentException("Expected at least 1 constructor of " + clazz.getCanonicalName() + " to have zero parameters, failed to find any.");
                }

                Command newInstance = (Command) constructor.newInstance();
                if (newInstance.flags.requireMaintenance && !Main.maintenance) {
                    continue;
                }

                Log.text("Registering command: /" + newInstance.name + " (class: " + clazz.getCanonicalName() + ")");
                commands.add(newInstance);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException error) {
                Log.error("An error occurred while registering commands: " + error, error);
            }
        }

        // register commands with JDA
        List<CommandData> allCommandData = new ArrayList<>();
        for (Command command : commands) {
            allCommandData.addAll(command.data);
        }
        Main.JDA.updateCommands().addCommands(allCommandData).queue();
    }




    private final String name;
    private final String description;
    private final CommandFlags flags;

    private final List<CommandData> data;

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

}
