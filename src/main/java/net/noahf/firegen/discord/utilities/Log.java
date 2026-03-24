package net.noahf.firegen.discord.utilities;

public class Log {

    public static void text(String message) {
        System.out.println(message);
    }

    public static void error(String message) {
        System.err.println(message);
    }

    public static void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace(System.err);
    }

}