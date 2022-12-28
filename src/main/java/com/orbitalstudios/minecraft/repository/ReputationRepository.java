package com.orbitalstudios.minecraft.repository;

import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
public class ReputationRepository {

    private final Map<UUID, ReputationPlayer> reputationPlayers = new Hashtable<>();

    @Nullable
    public ReputationPlayer getReputationPlayer(@NotNull UUID uuid) {
        return reputationPlayers.get(uuid);
    }

    @Nullable
    public ReputationPlayer getReputationPlayer(@NotNull String name) {
        return reputationPlayers.values()
            .stream()
            .filter(reputationPlayer -> {
                String playerName = reputationPlayer.getName();
                if (playerName == null) {
                    return false;
                }

                return playerName.equalsIgnoreCase(name);
            }).findFirst()
            .orElse(null);
    }

    public void putReputationPlayer(@NotNull ReputationPlayer reputationPlayer) {
        reputationPlayers.put(reputationPlayer.getUniqueId(), reputationPlayer);
    }

    public ReputationPlayer removeReputationPlayer(@NotNull UUID uniqueId) {
        return reputationPlayers.remove(uniqueId);
    }

    public Map<UUID, ReputationPlayer> getReputationPlayers() {
        return reputationPlayers;
    }
}
