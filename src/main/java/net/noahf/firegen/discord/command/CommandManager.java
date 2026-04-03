package net.noahf.firegen.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandManager extends ListenerAdapter {

    public static final String COMMANDS_PACKAGE = "net.noahf.firegen.discord.command.registered";

    private final List<Command> commands = new ArrayList<>();

    public CommandManager() {
        // find command classes and instantiate
        Set<Class<? extends Command>> classes = new Reflections(COMMANDS_PACKAGE).getSubTypesOf(Command.class);
        Log.info("Attempting to load and register " + classes.size() + " commands in " + COMMANDS_PACKAGE + "...");
        for (Class<? extends Command> clazz : classes) {
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
                    Log.warn("Not registering /" + newInstance.name + " because the command requires maintenance mode.");
                    continue;
                }

                Log.info("Registered: /" + newInstance.name + " (class: " + clazz.getCanonicalName() + ")");
                commands.add(newInstance);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException error) {
                Log.error("An error occurred while registering commands: " + error, error);
            }
        }
        // register commands with JDA
        Main.JDA.addEventListener(this);
        List<CommandData> allCommandData = new ArrayList<>();
        for (Command command : commands) {
            allCommandData.addAll(command.data);
        }
        Main.JDA.updateCommands().addCommands(allCommandData).queue();
    }

    public <T extends Command> T getCommandClass(Class<T> clazz) {
        Command cmd = null;
        for (Command c : this.commands) {
            if (c.getClass().getCanonicalName().equalsIgnoreCase(clazz.getCanonicalName()))
                continue;
            cmd = c;
            break;
        }
        if (cmd == null) {
            throw new IllegalArgumentException("Failed to find command by " + clazz.getCanonicalName());
        }
        return clazz.cast(cmd);
    }

    @SuppressWarnings("unchecked") // due to casting "cmd" to (T)
    public <T extends Command> @NotNull T getCommandName(String name) {
        Command cmd = null;
        for (Command c : this.commands) {
            if (c.name.equalsIgnoreCase(name)) {
                cmd = c;
                break;
            }
            if (c.flags.aliases != null && Arrays.stream(c.flags.aliases).filter(s -> s.equalsIgnoreCase(name)).findFirst().orElse(null) != null) {
                cmd = c;
                break;
            }
        }
        if (cmd == null) {
            throw new IllegalArgumentException("Failed to find command by name " + name);
        }
        return (T) cmd;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String commandString = event.getCommandString();
        Command command;

        Log.info(user.getName() + " (" + user.getIdLong() + "): /" + event.getFullCommandName());
        try {
            if (Main.maintenance && !Main.allowedDuringMaintenance.contains(user.getIdLong())) {
                // send maintenance status
                return;
            }

            command = this.getCommandName(event.getName());

            if (command.flags.requireMaintenance && !Main.maintenance) {
                // send requires maintenance status
                return;
            }

            command.command(event);
        } catch (Exception exception) {
            // failure occurred
            Log.error("An error occurred while executing '" +  commandString + "' for " + user.getName() + ": " + exception, exception);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        User user = event.getUser();
        String commandString = event.getCommandString();
        String optionValue = event.getFocusedOption().getValue();
        try {
            List<String> autocomplete = this
                    .getCommandName(event.getName())
                    .autocomplete(event, user, commandString, event.getFocusedOption());

            if (autocomplete == null || autocomplete.isEmpty()) {
                event.replyChoiceStrings().queue();
                return;
            }

            if (optionValue.isEmpty()) {
                event.replyChoiceStrings(autocomplete.stream().limit(25).toList()).queue();
                return;
            }

            List<String> returned = new ArrayList<>();
            for (String a : autocomplete) {
                if (a.toLowerCase().contains(optionValue.toLowerCase())) {
                    returned.add(a);
                }
            }
            event.replyChoiceStrings(returned.stream().limit(25).toList()).queue();
        } catch (Exception exception) {
            Log.error("Autocomplete failed for '" + commandString + "': " + exception, exception);
        }
    }
}
