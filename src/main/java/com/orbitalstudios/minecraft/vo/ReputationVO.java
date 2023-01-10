package com.orbitalstudios.minecraft.vo;

import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
public record ReputationVO(
    long dislikeCooldown,

    Map<VoteType, Float> reputationChange,
    Map<String, String> permissions,
    Map<VoteType, ChatColor> reputationColor,
    Map<String, String> messages
) {

    public Float getReputationChange(VoteType voteType) {
        return reputationChange.get(voteType);
    }

    public ChatColor getReputationColor(VoteType voteType) {
        return reputationColor.get(voteType);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, ChatColor.RED + "Seems like there's not message for '" + key + "' entry.");
    }

    public String getMessage(String key, Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        String message = messages.get(key);
        for (int index = 0; index < args.length; index += 2) {
            if (!(args[index] instanceof String string)) {
                throw new IllegalArgumentException("Invalid arguments");
            }

            message = message.replace(string, String.valueOf(args[index + 1]));
        }

        return message;
    }

    public boolean hasPermission(@NotNull CommandSender commandSender, @NotNull String permission) {
        return commandSender.hasPermission(permissions.getOrDefault(permission, "reputation." + permission));
    }

}
