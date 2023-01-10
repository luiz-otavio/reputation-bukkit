package com.orbitalstudios.minecraft.extensions;

import com.orbitalstudios.minecraft.ReputationPlugin;
import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.repository.ReputationRepository;
import com.orbitalstudios.minecraft.storage.ReputationStorage;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
@RequiredArgsConstructor
public class PercentageExtension extends PlaceholderExpansion {

    private final ReputationRepository reputationRepository;
    private final ReputationStorage reputationStorage;

    @Override
    public @NotNull String getIdentifier() {
        return "reputation-percentage";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Luiz O. F. Corrêa";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return ReputationPlugin.getInstance()
            .getName();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        ReputationPlayer reputationPlayer = reputationRepository.getReputationPlayer(player.getUniqueId());
        if (reputationPlayer == null) {
            reputationPlayer = reputationStorage.retrievePlayer(player.getUniqueId())
                .join();

            if (reputationPlayer != null) {
                reputationRepository.putReputationPlayer(reputationPlayer);
            } else {
                return null;
            }
        }

        int likes = reputationPlayer.getVotes(VoteType.LIKE),
            dislikes = reputationPlayer.getVotes(VoteType.DISLIKE),
            total = likes + dislikes;

        float unit = total / 100f,
            percentage = likes * unit;

        return String.valueOf(percentage);
    }
}
