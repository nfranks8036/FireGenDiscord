package net.noahf.firegen.discord.incidents.buttonaction.narrative;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.ButtonAction;
import net.noahf.firegen.discord.incidents.StringDropdownAction;
import net.noahf.firegen.discord.incidents.structure.Agency;
import net.noahf.firegen.discord.incidents.structure.Incident;

public class EditNarrative implements ButtonAction, StringDropdownAction {

    @Override
    public String getName() {
        return "editnarrative";
    }

    @Override
    public void execute(Incident incident, ButtonInteractionEvent event) {

    }

    @Override
    public void execute(Incident incident, StringSelectInteractionEvent event) {

    }

    @Override
    public void execute(Incident incident, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(incident, e); }
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(incident, e); }
    }
}
