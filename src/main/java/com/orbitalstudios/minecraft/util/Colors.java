package com.orbitalstudios.minecraft.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Luiz O. F. Corrêa
 * @since 01/01/2023
 **/
public class Colors {

    public static String getColor(@NotNull String color) {
        return ChatColor.translateAlternateColorCodes('&', color);
    }

}
