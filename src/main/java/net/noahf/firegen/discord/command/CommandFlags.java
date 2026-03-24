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
public class CommandFlags {

    public static CommandFlags none() {
        return include().finish();
    }

    public final String[] aliases;
    public final OptionData[] options;
    public final DefaultMemberPermissions permissions;
    public final boolean requireMaintenance;

}
