package com.orbitalstudios.minecraft.storage.impl;

import com.orbitalstudios.minecraft.logger.ReputationLogger;
import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.storage.ReputationStorage;
import com.orbitalstudios.minecraft.storage.connector.ReputationStorageConnector;
import com.orbitalstudios.minecraft.util.SQLReader;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
public class SQLReputationStorage implements ReputationStorage {

    private final ReputationStorageConnector connector;
    private final SQLReader sqlReader;

    public SQLReputationStorage(ReputationStorageConnector connector) {
        this.connector = connector;

        this.sqlReader = new SQLReader();
        try {
            sqlReader.loadFromResources();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        try (Connection connection = connector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                sqlReader.getSql("create_users_table")
            );

            int result = preparedStatement.executeUpdate();

            if (result == 0) {
                ReputationLogger.info("Database table created successfully.");
            } else {
                ReputationLogger.info("Database table already exists.");
            }

            preparedStatement = connection.prepareStatement(
                sqlReader.getSql("create_votes_table")
            );

            result = preparedStatement.executeUpdate();

            if (result == 0) {
                ReputationLogger.info("Database table created successfully.");
            } else {
                ReputationLogger.info("Database table already exists.");
            }
        } catch (Exception exception) {
            ReputationLogger.error("Failed to connect to database", exception);
        }
    }

    @Override
    public ReputationStorageConnector getConnector() {
        return connector;
    }

    @Override
    public SQLReader getReader() {
        return sqlReader;
    }

    @Override
    public CompletableFuture<ReputationPlayer> retrievePlayer(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connector.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                    sqlReader.getSql("retrieve_user_by_uuid")
                );

                preparedStatement.setString(1, uuid.toString());

                ResultSet resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    ReputationLogger.info("Player not found in database.");
                    return null;
                }

                ReputationLogger.info("Player found in database.");
                EnumMap<VoteType, Integer> votes = new EnumMap<>(VoteType.class);
                EnumMap<VoteType, Instant> history = new EnumMap<>(VoteType.class);

                for (VoteType voteType : VoteType.values()) {
                    String lowerCase = voteType.name()
                        .toLowerCase();

                    int votesValue = resultSet.getInt(lowerCase);

                    Instant historyValue = resultSet.getTimestamp("last_" + lowerCase)
                        .toInstant();

                    votes.put(voteType, votesValue);
                    history.put(voteType, historyValue);
                }

                Instant createdAt = resultSet.getTimestamp("created_at")
                    .toInstant();

                Instant updatedAt = resultSet.getTimestamp("updated_at")
                    .toInstant();

                String playerName = resultSet.getString("player_name");

                return new ReputationPlayer(uuid, playerName, votes, history, createdAt, updatedAt);
            } catch (Exception exception) {
                ReputationLogger.error("Failed to retrieve player", exception);
            }

            return null;
        }, connector.getWorker());
    }

    @Override
    public CompletableFuture<ReputationPlayer> retrievePlayer(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connector.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                    sqlReader.getSql("retrieve_user_by_player_name")
                );

                preparedStatement.setString(1, name);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    ReputationLogger.info("Player not found in database.");
                    return null;
                }

                ReputationLogger.info("Player found in database.");
                EnumMap<VoteType, Integer> votes = new EnumMap<>(VoteType.class);
                EnumMap<VoteType, Instant> history = new EnumMap<>(VoteType.class);

                for (VoteType voteType : VoteType.values()) {
                    String lowerCase = voteType.name()
                        .toLowerCase();

                    int votesValue = resultSet.getInt(lowerCase);

                    Instant historyValue = resultSet.getTimestamp("last_" + lowerCase)
                        .toInstant();

                    votes.put(voteType, votesValue);
                    history.put(voteType, historyValue);
                }

                Instant createdAt = resultSet.getTimestamp("created_at")
                    .toInstant();

                Instant updatedAt = resultSet.getTimestamp("updated_at")
                    .toInstant();

                UUID uniqueId = UUID.fromString(resultSet.getString("player_uuid"));

                return new ReputationPlayer(uniqueId, name, votes, history, createdAt, updatedAt);
            } catch (Exception exception) {
                ReputationLogger.error("Failed to retrieve player", exception);
            }

            return null;
        }, connector.getWorker());
    }

    @Override
    public CompletableFuture<ReputationPlayer> createPlayer(@NotNull UUID uuid, @NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connector.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                    sqlReader.getSql("insert_user_by_uuid")
                );

                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, name);

                int result = preparedStatement.executeUpdate();
                if (result == 0) {
                    ReputationLogger.info("Player not created in database.");
                    return null;
                }

                ReputationLogger.info("Player created in database.");
                return new ReputationPlayer(
                    uuid,
                    name,
                    new EnumMap<>(VoteType.class),
                    new EnumMap<>(VoteType.class),
                    Instant.now(),
                    Instant.now()
                );
            } catch (Exception exception) {
                ReputationLogger.error("Failed to create player", exception);
            }

            return null;
        }, connector.getWorker());
    }

    @Override
    public CompletableFuture<Void> updatePlayer(@NotNull ReputationPlayer player) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = connector.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                    sqlReader.getSql("update_user_by_uuid")
                );

                preparedStatement.setString(1, player.getName());

                Instant likeHistory = player.getHistory(VoteType.DISLIKE),
                    dislikeHistory = player.getHistory(VoteType.LIKE);

                preparedStatement.setTimestamp(2, likeHistory == null ? null : Timestamp.from(likeHistory));
                preparedStatement.setTimestamp(3, dislikeHistory == null ? null : Timestamp.from(dislikeHistory));

                preparedStatement.setTimestamp(
                    4,
                    Timestamp.from(player.getUpdatedAt())
                );

                preparedStatement.setString(5, player.getUniqueId().toString());

                int result = preparedStatement.executeUpdate();
                if (result == 0) {
                    ReputationLogger.info("Player not updated in database.");
                }

                ReputationLogger.info("Player updated in database.");
            } catch (Exception exception) {
                ReputationLogger.error("Failed to update player", exception);
            }
            ;
        }, connector.getWorker());
    }

    @Override
    public CompletableFuture<Boolean> computeVote(@NotNull ReputationPlayer player, @NotNull ReputationPlayer target, @NotNull VoteType voteType) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connector.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                    sqlReader.getSql("insert_vote_log")
                );

                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.setString(2, player.getName());

                preparedStatement.setString(3, target.getUniqueId().toString());
                preparedStatement.setString(4, target.getName());

                preparedStatement.setString(5, voteType.name());

                int result = preparedStatement.executeUpdate();
                if (result == 0) {
                    ReputationLogger.info("Vote not computed in database.");
                    return false;
                }

                ReputationLogger.info("Vote computed in database.");
                return true;
            } catch (Exception exception) {
                ReputationLogger.error("Failed to compute vote", exception);
            }

            return false;
        }, connector.getWorker());
    }

    @Override
    public CompletableFuture<Boolean> hasVoted(@NotNull ReputationPlayer player, long seconds) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connector.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                    sqlReader.getSql("check_vote_by_cooldown")
                );

                long time = System.currentTimeMillis() - (seconds * 1000);

                preparedStatement.setString(1, player.getUniqueId().toString());

                Timestamp timestamp = Timestamp.from(
                    Instant.ofEpochMilli(time)
                );

                preparedStatement.setTimestamp(2, timestamp);

                return preparedStatement.executeQuery()
                    .next();
            } catch (Exception exception) {
                ReputationLogger.error("Failed to retrieve vote log", exception);
            }

            return false;
        }, connector.getWorker());
    }

    @Override
    public CompletableFuture<Boolean> batchPlayers(@NotNull ReputationPlayer... players) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connector.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                    sqlReader.getSql("insert_user_by_uuid")
                );

                for (ReputationPlayer player : players) {
                    preparedStatement.setString(1, player.getName());

                    Instant likeHistory = player.getHistory(VoteType.DISLIKE),
                        dislikeHistory = player.getHistory(VoteType.LIKE);

                    preparedStatement.setTimestamp(2, likeHistory == null ? null : Timestamp.from(likeHistory));
                    preparedStatement.setTimestamp(3, dislikeHistory == null ? null : Timestamp.from(dislikeHistory));

                    preparedStatement.setTimestamp(
                        4,
                        Timestamp.from(player.getUpdatedAt())
                    );

                    preparedStatement.setString(5, player.getUniqueId().toString());
                }

                int[] result = preparedStatement.executeBatch();
                if (result.length == 0) {
                    ReputationLogger.info("Players not batched in database.");
                    return true;
                }

                ReputationLogger.info("Players batched in database.");
                return true;
            } catch (Exception exception) {
                ReputationLogger.error("Failed to batch players", exception);
            }

            return false;
        }, connector.getWorker());
    }
}
