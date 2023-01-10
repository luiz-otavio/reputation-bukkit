package com.orbitalstudios.minecraft.util;

/**
 * @author Luiz O. F. Corrêa
 * @since 10/01/2023
 **/
public class Formatter {

    public static String format(long cooldown) {
        long seconds = cooldown % 60,
            minutes = seconds % 60,
            hours = minutes % 24;

        if (hours > 0) {
            return String.format("%d hours, %d minutes and %d seconds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d minutes and %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }


}
