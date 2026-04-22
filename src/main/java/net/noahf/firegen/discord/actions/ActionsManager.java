package net.noahf.firegen.discord.actions;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.errors.ActionCommandNotExist;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.utilities.Log;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionsManager {

    public static final String ACTIONS_PACKAGE = "net.noahf.firegen.discord.actions.registered";

    private @Getter List<FireGenAction> actions = new ArrayList<>();

    public ActionsManager() {
        // find command classes and instantiate
        Set<Class<? extends FireGenAction>> classes =
                new Reflections(ACTIONS_PACKAGE)
                        .getSubTypesOf(FireGenAction.class).stream()
                        .filter(c -> !c.isInterface())
                        .collect(Collectors.toSet());

        Log.info("Attempting to load and register " + classes.size() + " actions in " + ACTIONS_PACKAGE + "...");

        for (Class<? extends FireGenAction> clazz : classes) {

            try {

                @Nullable Constructor<?> constructor =
                        Arrays.stream(clazz.getDeclaredConstructors())
                                // since we don't really know what other constructors exist, we can only confidently
                                // instantiate a constructor with zero parameters.
                                .filter(c -> c.getParameterCount() == 0)

                                .findFirst().orElse(null);

                if (constructor == null) {
                    throw new IllegalArgumentException("Expected at least one constructor of " +
                            clazz.getCanonicalName() + " to have zero parameters, failed to find any.");
                }

                FireGenAction newInstance = (FireGenAction) constructor.newInstance();

                Log.info("Registered action '" + newInstance.getName() + "' (class: " + clazz.getCanonicalName() + ")");
                actions.add(newInstance);

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException error) {
                Log.error("An error occurred while registering actions: " + error, error);
            }

        }
    }

    public void processAction(GenericInteractionCreateEvent event, String id) {
        String[] sections = id.split("-");

        if (sections.length < 3) {
            throw new IllegalArgumentException("Returned ID does not match known format: '" + id + "'");
        }

        String incidentNumber = sections[1];
        String actionTitle = sections[2];
        String[] params = Arrays.copyOfRange(sections, 3, sections.length);

        IncidentImpl incident = Main.incidents.getIncidentBy(Long.parseLong(incidentNumber));
        if (incident == null) {
            throw new IllegalArgumentException("Incident with ID '" + incidentNumber + "' does not exist.");
        }

        Log.info("Searching for action '" + actionTitle + "'");
        for (FireGenAction action : this.actions) {
            if (!action.getName().equalsIgnoreCase(actionTitle)) {
                continue;
            }

            ActionsContext context = new ActionsContext(
                    Main.incidents,
                    incident,
                    actionTitle,
                    Arrays.asList(params)
            );

            action.execute(context, event);
            return;
        }

        throw new ActionCommandNotExist(actionTitle);
    }

}
