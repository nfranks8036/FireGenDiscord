package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.noahf.firegen.api.incidents.units.AgencyType;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.utilities.IdGenerator;

import java.util.List;

@Getter
@AllArgsConstructor
public class AgencyImpl implements net.noahf.firegen.api.incidents.units.Agency {

    private String shorthand;
    private String longhand;
    private String formatted;
    private String emoji;
    private AgencyType type;
    private List<Unit> units;

    private SelectOption selectOption;

    @Override
    public String toString() {
        return this.shorthand;
    }

    @Override
    public long getId() {
        return IdGenerator.generateAgencyId(this);
    }
}
