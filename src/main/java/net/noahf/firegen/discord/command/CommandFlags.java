package net.noahf.firegen.discord.command;

import lombok.AccessLevel;
import lombok.Builder;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Builder(
        access = AccessLevel.PUBLIC,
        builderMethodName = "include",
        buildMethodName = "finish"
)
/*
 * Additional command flags to change the behavior of how the command is registered with Discord
 */
public class CommandFlags {

    /**
     * Include zero command flags (essentially using default / no extra options)
     * @return the {@link CommandFlags} with zero options
     */
    public static CommandFlags none() {
        return include().finish();
    }

    /**
     * The aliases for the command, which are essentially extra ways to access the command apart from the primary
     * command name
     */
    public final String[] aliases;

    /**
     * The {@link OptionData command's options}, which are essentially parameters the user can enter into the command
     */
    public final OptionData[] options;

    /**
     * The {@link DefaultMemberPermissions default permissions} required to execute the command, which is fed to Discord
     */
    public final DefaultMemberPermissions permissions;

//    /**
//     * If the command can only be accessed while maintenance mode is active, {@code true} means the command will only
//     * work if maintenance mode is active
//     */
//    public final boolean requireMaintenance;

}
