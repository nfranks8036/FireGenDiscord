package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.components.selections.SelectOption;

@AllArgsConstructor
public class Agency {

    private @Getter String shorthand;
    private @Getter String longhand;
    private @Getter String formatted;
    private @Getter String emoji;

    private @Getter SelectOption selectOption;

    @Override
    public String toString() {
        return shorthand;
    }
}
