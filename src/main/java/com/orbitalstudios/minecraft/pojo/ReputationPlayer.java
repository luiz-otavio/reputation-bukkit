package com.orbitalstudios.minecraft.pojo;

import com.orbitalstudios.minecraft.event.impl.PlayerUpdateReputationEvent;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.vo.ReputationVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
@AllArgsConstructor
@Getter
public class ReputationPlayer {

    private final UUID uniqueId;
    private final String name;

    private final Map<VoteType, Integer> votes;
    private final Map<VoteType, Instant> histories;

    private final Instant createdAt;

    private Instant updatedAt;

    @Nullable
    public Player getPlayer() {
        if (uniqueId == null) {
            return Bukkit.getPlayerExact(name);
        } else {
            return Bukkit.getPlayer(uniqueId);
        }
    }

    public void computeVote(VoteType voteType) {
        PlayerUpdateReputationEvent event = new PlayerUpdateReputationEvent(
            this,
            this,
            voteType,
            votes.getOrDefault(voteType, 0) + 1
        ).call();

        if (event.isCancelled()) {
            return;
        }

        updatedAt = Instant.now();

        votes.put(voteType, event.getReputation());
    }

    public int getVotes(VoteType voteType) {
        return votes.getOrDefault(voteType, 0);
    }

    public void setVotes(VoteType voteType, int amount) {
        votes.put(voteType, amount);
    }

    public Instant getHistory(VoteType voteType) {
        return histories.get(voteType);
    }

    public float getReputation(@NotNull ReputationVO reputationVO) {
        float reputation = 0.0F;

        reputation += votes.getOrDefault(VoteType.LIKE, 0) * reputationVO.getReputationChange(VoteType.LIKE);
        reputation -= votes.getOrDefault(VoteType.DISLIKE, 0) * reputationVO.getReputationChange(VoteType.DISLIKE);

        return reputation;
    }

}
