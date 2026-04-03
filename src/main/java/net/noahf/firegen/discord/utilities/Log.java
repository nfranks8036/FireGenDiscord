package net.noahf.firegen.discord.utilities;

import net.noahf.firegen.discord.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.noahf.firegen.discord.Main.LOGGER;

public class Log {

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

}