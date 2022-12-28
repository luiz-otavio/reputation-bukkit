package com.orbitalstudios.minecraft.extensions;

import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.repository.ReputationRepository;
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

    @Override
    public @NotNull String getIdentifier() {
        return "reputation_likes";
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
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        ReputationPlayer reputationPlayer = reputationRepository.getReputationPlayer(player.getUniqueId());
        if (reputationPlayer == null) {
            return null;
        }

        int likes = reputationPlayer.getVotes(VoteType.LIKE),
            dislikes = reputationPlayer.getVotes(VoteType.DISLIKE),
            total = likes + dislikes;

        float unit = total / 100f,
            percentage = likes * unit;

        return String.valueOf(percentage);
    }
}
