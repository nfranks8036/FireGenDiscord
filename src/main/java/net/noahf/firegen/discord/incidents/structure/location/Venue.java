package net.noahf.firegen.discord.incidents.structure.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.components.Component;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class Venue {

    private final @Getter String name;
    private final @Getter String display;

    @Override
    public String toString() {
        return this.name;
    }

    static final int COMMON_NAME_ID = Integer.MAX_VALUE / 2;
    static final int VENUE_ID = (Integer.MAX_VALUE / 2) + 1;

    static final Component COMMON_NAME_COMPONENT = new net.dv8tion.jda.api.components.Component() {
        @NotNull @Override public Type getType() { return Type.LABEL; }
        @NotNull @Override public Component withUniqueId(int uniqueId) { return this; }

        @Override
        public int getUniqueId() {
            return COMMON_NAME_ID;
        }
    };

    static final Component VENUE_COMPONENT = new Component() {
        @NotNull @Override public Type getType() { return Type.LABEL; }
        @NotNull @Override public Component withUniqueId(int uniqueId) { return this; }

        @Override
        public int getUniqueId() {
            return VENUE_ID;
        }
    };

}