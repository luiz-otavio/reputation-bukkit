package com.orbitalstudios.minecraft.storage.impl;

import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.storage.ReputationStorage;
import com.orbitalstudios.minecraft.storage.connector.ReputationStorageConnector;
import com.orbitalstudios.minecraft.util.SQLReader;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 04/01/2023
 **/
public class DevelopmentReputationStorage implements ReputationStorage {

    @Override
    public ReputationStorageConnector getConnector() {
        return null;
    }

    @Override
    public SQLReader getReader() {
        return null;
    }

    @Override
    public CompletableFuture<ReputationPlayer> retrievePlayer(@NotNull UUID uuid) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ReputationPlayer> retrievePlayer(@NotNull String name) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ReputationPlayer> createPlayer(@NotNull UUID uuid, @NotNull String name) {
        return CompletableFuture.completedFuture(
            new ReputationPlayer(
                uuid,
                name,
                new EnumMap<>(VoteType.class),
                new EnumMap<>(VoteType.class),
                Instant.now(),
                Instant.now()
            )
        );
    }

    @Override
    public CompletableFuture<Void> updatePlayer(@NotNull ReputationPlayer player) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> computeVote(@NotNull ReputationPlayer player, @NotNull ReputationPlayer target, @NotNull VoteType voteType) {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> hasVoted(@NotNull ReputationPlayer player, long seconds) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> batchPlayers(@NotNull ReputationPlayer... players) {
        return CompletableFuture.completedFuture(true);
    }
}
