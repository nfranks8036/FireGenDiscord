package net.noahf.firegen.discord.incidents;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class Agency {

    private @Getter String agencyText;
    private @Getter @Setter(value = AccessLevel.PACKAGE) String agencyShorthand;
}
