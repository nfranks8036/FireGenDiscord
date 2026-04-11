package net.noahf.firegen.discord.utilities;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class Time {

    public static long getUnix(LocalDateTime time) {
        return time.toEpochSecond(OffsetDateTime.now().getOffset());
    }

    public static long getUnix() {
        return getUnix(LocalDateTime.now());
    }

    public static long getUnixOffset(int offset, TimeUnit unit) {
        LocalDateTime time = LocalDateTime.now();
        if (offset > 0) {
            time = time.plus(offset, unit.toChronoUnit());
        } else if (offset < 0) {
            time = time.minus(offset, unit.toChronoUnit());
        }
        return getUnix(time);
    }

}
