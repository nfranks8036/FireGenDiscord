package net.noahf.firegen.discord.incidents.structure;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Agency {

    private @Getter @Setter(value = AccessLevel.PACKAGE) String agencyLong;
    private @Getter @Setter(value = AccessLevel.PACKAGE) String agencyFormatted;
    private @Getter @Setter(value = AccessLevel.PACKAGE) String agencyShorthand;
}
