package com.orbitalstudios.minecraft.storage;

import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.storage.connector.ReputationStorageConnector;
import com.orbitalstudios.minecraft.util.SQLReader;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
public interface ReputationStorage {

    ReputationStorageConnector getConnector();

    SQLReader getReader();

    CompletableFuture<ReputationPlayer> retrievePlayer(@NotNull UUID uuid);

    CompletableFuture<ReputationPlayer> retrievePlayer(@NotNull String name);

    CompletableFuture<ReputationPlayer> createPlayer(@NotNull UUID uuid, @NotNull String name);

    CompletableFuture<Void> updatePlayer(@NotNull ReputationPlayer player);

    CompletableFuture<Boolean> computeVote(
        @NotNull ReputationPlayer player,
        @NotNull ReputationPlayer target,
        @NotNull VoteType voteType
    );

    CompletableFuture<Boolean> updateVotes(
        @NotNull ReputationPlayer reputationPlayer,
        @NotNull VoteType voteType,
        int amount
    );

    CompletableFuture<Boolean> hasVoted(@NotNull ReputationPlayer player, @NotNull ReputationPlayer target, long seconds);

    CompletableFuture<Boolean> batchPlayers(@NotNull ReputationPlayer... players);

}
