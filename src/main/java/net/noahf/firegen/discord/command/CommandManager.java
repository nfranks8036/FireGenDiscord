package net.noahf.firegen.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.DiscordMessages;
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

/**
 * Represents the Command Manager, the conduit of information between any commands, including how the commands get
 * executed, how the commands are registered, how the commands are stored, and how the commands are accessed.
 */
public class CommandManager extends ListenerAdapter {

    /**
     * This is where all the command classes are registered, only classes of packages or sub-packages will be noticed.
     */
    public static final String COMMANDS_PACKAGE = "net.noahf.firegen.discord.command.registered";

    private final List<Command> commands = new ArrayList<>();

    /**
     * The task of the constructor of the {@link CommandManager} is to find, register, and communicate with the
     * commands in the package. The constructor also registers the command with JDA and subsequently Discord.
     */
    public CommandManager() {
        // find command classes and instantiate
        Set<Class<? extends Command>> classes =
                new Reflections(COMMANDS_PACKAGE)
                        .getSubTypesOf(Command.class); // we only care about those which extend the Command class

        Log.info("Attempting to load and register " + classes.size() + " commands in " + COMMANDS_PACKAGE + "...");

        for (Class<? extends Command> clazz : classes) {

            try {

                @Nullable Constructor<?> constructor =
                        Arrays.stream(clazz.getDeclaredConstructors())
                                // since we don't really know what other constructors exist, we can only confidently
                                // instantiate a constructor with zero parameters.
                                .filter(c -> c.getParameterCount() == 0)

                                .findFirst().orElse(null);

                if (constructor == null) {
                    throw new IllegalArgumentException("Expected at least one constructor of " + clazz.getCanonicalName() + " to have zero parameters, failed to find any.");
                }

                Command newInstance = (Command) constructor.newInstance();
//                if (newInstance.flags.requireMaintenance && !Main.maintenance) {
//                    // if maintenance is off but the command calls for maintenance mode, we can just ignore it and not register it
//                    // this is especially since maintenance mode cannot be toggled on in runtime, the program must be
//                    // re-executed and restarted
//                    Log.warn("Not registering /" + newInstance.name + " because the command requires maintenance mode.");
//                    continue;
//                }

                Log.info("Registered: /" + newInstance.name + " (class: " + clazz.getCanonicalName() + ")");
                commands.add(newInstance);

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException error) {
                Log.error("An error occurred while registering commands: " + error, error);
            }

        }

        // register commands with JDA and subsequently Discord
        Main.JDA.addEventListener(this);
        List<CommandData> allCommandData = new ArrayList<>();
        for (Command command : commands) {
            allCommandData.addAll(command.data);
        }

        Main.JDA.updateCommands().addCommands(allCommandData).queue((c) ->
                Log.info("Successfully registered all commands with JDA, returned with " + (c == null ? "null" : c.size()) + " commands.")
        );
    }

    /**
     * Gets a command given a certain class, this must extend {@link Command}
     * @param clazz the class to get. This should be unique per command.
     * @return the instance of the command that is associated with the {@link Class class}
     * @param <T> the type parameter of that command
     */
    public <T extends Command> T getCommandClass(Class<T> clazz) {
        Command cmd = null;
        for (Command c : this.commands) {
            if (!c.getClass().getCanonicalName().equalsIgnoreCase(clazz.getCanonicalName()))
                continue;
            return clazz.cast(c);
        }
        // if we don't return sooner, then we must not have found a command that matched the class
        throw new IllegalArgumentException("Failed to find command by " + clazz.getCanonicalName());
    }

    /**
     * Gets a command given a certain name or alias.
     * @param name the name of the command, can also be an alias of a command, case-insensitive
     * @return the instance of the command that is associated with the name or alias
     * @param <T> the type parameter of that command
     */
    @SuppressWarnings("unchecked") // due to casting "cmd" to (T)
    public <T extends Command> @NotNull T getCommandName(String name) {
        for (Command c : this.commands) {
            if (c.name.equalsIgnoreCase(name)) {
                return (T) c;
            }
            if (c.flags.aliases != null && Arrays.stream(c.flags.aliases).filter(s -> s.equalsIgnoreCase(name)).findFirst().orElse(null) != null) {
                return (T) c;
            }
        }
        // if we didn't return quicker than this, then we must have found a command that matched this class
        throw new IllegalArgumentException("Failed to find command by name " + name);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String commandString = event.getCommandString();
        Command command;

        Log.info(user.getName() + " (" + user.getIdLong() + "): /" + event.getFullCommandName());
        try {

//            if (Main.maintenance && !Main.allowedDuringMaintenance.contains(user.getIdLong())) {
//                // send maintenance status if we're in maintenance mode and the user is not allowed to use this command
//                DiscordMessages.error(event, "The Discord bot is currently in maintenance mode! Try again later.");
//                return;
//            }

            command = this.getCommandName(event.getName());
            command.command(event);
        } catch (Exception exception) {
            // failure occurred, oh nose
            Log.error("An error occurred while executing '" +  commandString + "' for " + user.getName() + ": " + exception, exception);
            DiscordMessages.error(event, "An error occurred while executing this command. Your request could not be fulfilled.", exception);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        User user = event.getUser();
        String commandString = event.getCommandString();
        String optionValue = event.getFocusedOption().getValue();

        try {
            // execute the autocomplete in the Command class to get the responses the class wants
            List<String> autocomplete = this
                    .getCommandName(event.getName())
                    .autocomplete(event, user, commandString, event.getFocusedOption());

            // we can just return nothing if the command returned null or an empty set
            if (autocomplete == null || autocomplete.isEmpty()) {
                event.replyChoiceStrings().queue();
                return;
            }

            // if the current option value is emptey but the command returned some valid options, then we can show as
            // many as Discord will allow (25 at the time of writing)
            if (optionValue.isEmpty()) {
                event.replyChoiceStrings(autocomplete.stream().limit(OptionData.MAX_CHOICES).toList()).queue();
                return;
            }

            // to better filter the data returned by the command if there is text written in the box, we will simply
            // look for options that contain the value the user typed in
            // toLowerCasing each string to effectively make it case-insensitive
            List<String> returned = new ArrayList<>();
            for (String a : autocomplete) {
                if (a.toLowerCase().contains(optionValue.toLowerCase())) {
                    returned.add(a);
                }
            }

            event.replyChoiceStrings(returned.stream().limit(OptionData.MAX_CHOICES).toList()).queue();

        } catch (Exception exception) {
            Log.error("Autocomplete failed for '" + commandString + "': " + exception, exception);
        }
    }
}
