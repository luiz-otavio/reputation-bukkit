package com.orbitalstudios.minecraft.util;

import java.util.concurrent.TimeUnit;

/**
 * @author Luiz O. F. Corrêa
 * @since 10/01/2023
 **/
public class Formatter {

    public static String format(long millis) {
        long seconds = millis / 1000,
            minutes = seconds / 60,
            hours = minutes / 60;

        if (hours > 0) {
            return String.format("%d hours, %d minutes and %d seconds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d minutes and %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }


}
