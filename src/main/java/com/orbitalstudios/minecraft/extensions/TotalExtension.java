package com.orbitalstudios.minecraft.extensions;

import com.orbitalstudios.minecraft.ReputationPlugin;
import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.repository.ReputationRepository;
import com.orbitalstudios.minecraft.vo.ReputationVO;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
@RequiredArgsConstructor
public class TotalExtension extends PlaceholderExpansion {

    private final ReputationRepository reputationRepository;
    private final ReputationVO reputationVO;

    @Override
    public @NotNull String getIdentifier() {
        return "reputation-total";
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
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        return super.onRequest(player, params);
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        ReputationPlayer reputationPlayer = reputationRepository.getReputationPlayer(player.getUniqueId());

        if (reputationPlayer == null) {
            return null;
        }

        return String.valueOf(
            reputationPlayer.getReputation(reputationVO)
        );
    }
}
