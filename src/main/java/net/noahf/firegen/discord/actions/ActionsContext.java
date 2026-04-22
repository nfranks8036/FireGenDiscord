package net.noahf.firegen.discord.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

import java.util.List;

/**
 * Represents a context in which a {@link FireGenAction} occurs in.
 * @see ActionsContext#getManager()
 * @see ActionsContext#getIncident()
 * @see ActionsContext#getCommand()
 * @see ActionsContext#getParameters()
 */
@AllArgsConstructor
public class ActionsContext {

    /**
     * Represents the current FireGen initiated of {@link IncidentManager} for quick-reference reasons.
     * This is also obtainable at {@link net.noahf.firegen.discord.Main#incidents Main.incidents}
     * @see ActionsContext
     */
    private @Getter IncidentManager manager;

    /**
     * Represents the current {@link IncidentImpl} that an action is being applied to.
     * @see ActionsContext
     */
    private @Getter IncidentImpl incident;

    /**
     * Represents the current {@link String command} that is being executed. This is usually provided in the class by
     * the method {@link FireGenAction#getName() getName()}.
     * @see ActionsContext
     */
    private @Getter String command;

    /**
     * Represents any additional parameters provided by the calling method. This can include additional information and
     * context to facilitate easy transfer-of-information.
     * @see ActionsContext
     */
    private @Getter List<String> parameters;

}
