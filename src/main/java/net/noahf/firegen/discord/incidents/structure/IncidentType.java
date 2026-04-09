package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class IncidentType {

    private @Getter String type;
    private @Getter IncidentTypeTag tag;
    private @Getter int qualifierChoice;

    public String getCompleteName() {
        return tag.fromType(this.type).get(this.qualifierChoice);
    }

}
