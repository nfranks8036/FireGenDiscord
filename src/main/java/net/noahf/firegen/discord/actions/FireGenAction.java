package net.noahf.firegen.discord.actions;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

import java.util.Arrays;

/**
 * Represents a generic FireGen action.
 * To be autoregistered, the action must be located in the {@link ActionsManager#ACTIONS_PACKAGE actions package}.
 */
public interface FireGenAction {

    String getName();

    void execute(ActionsContext ctx, GenericInteractionCreateEvent event);

    /**
     * Creates a customized callback ID number generally used for JDA interactions that can be used to get to the same
     * class again under a different action type.
     * @param context the {@link ActionsContext} under which the current action is running.
     * @param additionalParameters OPTIONAL: any additional parameters to facilitate information transfer,
     *                             obtainable with the {@link ActionsContext#getParameters() context.getParameters()}
     * @return the string callback ID with all the information requested, including incident number and parameters
     */
    default String callbackId(ActionsContext context, String... additionalParameters) {
        if (additionalParameters == null || additionalParameters.length == 0) {
            return context.getIncident().createInteractionIdString(this.getName());
        }

        String[] commands = Arrays.copyOf(additionalParameters, additionalParameters.length + 1);
        for (int i = 0; i < commands.length - 1; i++) {
            commands[i + 1] = commands[i];
        }
        commands[0] = this.getName();
        // the name of the command has to come first
        return context.getIncident().createInteractionIdString(commands);
    }

}
