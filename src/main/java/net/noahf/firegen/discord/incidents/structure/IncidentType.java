package net.noahf.firegen.discord.incidents.structure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@RequiredArgsConstructor
public class IncidentType {

    private @NotNull @Getter String type;
    private @NotNull @Getter IncidentTypeTag tag;
    private @NotNull @Getter int qualifierChoice;

    private @Getter long id = new Random().nextLong(1000000, 9999999);

    public String getCompleteName() {
        return tag.fromType(this.type).get(this.qualifierChoice);
    }

}
