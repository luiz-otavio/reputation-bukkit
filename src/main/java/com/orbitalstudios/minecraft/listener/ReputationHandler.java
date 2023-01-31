package com.orbitalstudios.minecraft.listener;

import com.orbitalstudios.minecraft.ReputationPlugin;
import com.orbitalstudios.minecraft.logger.ReputationLogger;
import com.orbitalstudios.minecraft.repository.ReputationRepository;
import com.orbitalstudios.minecraft.storage.ReputationStorage;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
@RequiredArgsConstructor
public class ReputationHandler implements Listener {

    private final ReputationRepository reputationRepository;
    private final ReputationStorage reputationStorage;

    private final ReputationPlugin reputationPlugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uniqueId = event.getUniqueId();

        reputationStorage.retrievePlayer(uniqueId)
            .thenAccept(reputationPlayer -> {
                if (reputationPlayer == null) {
                    reputationPlayer = reputationStorage.createPlayer(uniqueId, event.getName())
                        .join();
                } else {
                    if (!reputationPlayer.getName().equals(event.getName())) {
                        reputationPlayer.setName(event.getName());
                    }
                }

                reputationRepository.putReputationPlayer(reputationPlayer);
            });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (reputationPlugin.isRunning()) {
            reputationStorage.updatePlayer(
                reputationRepository.removeReputationPlayer(player.getUniqueId())
            ).thenAccept(unused -> {
                ReputationLogger.info("Player " + player.getName() + " has been saved!");
            });
        }
    }

}
