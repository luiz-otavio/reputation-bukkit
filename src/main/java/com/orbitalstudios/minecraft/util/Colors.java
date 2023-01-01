package com.orbitalstudios.minecraft.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Luiz O. F. Corrêa
 * @since 01/01/2023
 **/
public class Colors {

    public static ChatColor getColor(@NotNull String color) {
        String withoutChar = color.replace("&", "");

        ChatColor chatColor;
        for (ChatColor value : ChatColor.values()) {
            if (value.getChar() == withoutChar.charAt(0)) {
                chatColor = value;
                return chatColor;
            }
        }

        return ChatColor.WHITE;
    }

}
