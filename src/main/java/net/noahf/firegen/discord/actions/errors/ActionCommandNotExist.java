package net.noahf.firegen.discord.actions.errors;

import net.noahf.firegen.discord.actions.FireGenAction;

/**
 * Represents an error that occurs when the obtained action command (such as 'abcdefghi') does not match the valid
 * list of {@link FireGenAction commands}
 * (such as 'addnarrative', 'location', etc.)
 */
public class ActionCommandNotExist extends IllegalArgumentException {

    /**
     * Creates a new action command does not exist error
     * @param gotAction the received action, which did not exist upon checking
     */
    public ActionCommandNotExist(String gotAction) {
        super("The action entered '" + gotAction + "' does not exist as a command.");
    }

}
