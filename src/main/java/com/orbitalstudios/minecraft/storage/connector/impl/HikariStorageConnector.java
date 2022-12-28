package com.orbitalstudios.minecraft.storage.connector.impl;

import com.orbitalstudios.minecraft.logger.ReputationLogger;
import com.orbitalstudios.minecraft.storage.connector.ReputationStorageConnector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
public class HikariStorageConnector implements ReputationStorageConnector {

    private final Properties properties;

    private HikariConfig hikariConfig;
    private HikariDataSource hikariDataSource;

    private ExecutorService executorService;

    public HikariStorageConnector(@NotNull Properties properties) {
        this.properties = properties;

        this.hikariConfig = new HikariConfig(properties);
        if (hikariConfig.getMaximumPoolSize() < 1) {
            hikariConfig.setMaximumPoolSize(3);
        }
    }

    @Override
    public @NotNull CompletableFuture<Boolean> connect() {
        int poolSize = hikariConfig.getMaximumPoolSize();

        ReputationLogger.info("Connecting to database with a pool size of " + poolSize + "...");
        this.hikariDataSource = new HikariDataSource(hikariConfig);
        try(Connection connection = hikariDataSource.getConnection()) {
            if (!connection.isValid(5)) {
                ReputationLogger.error("Failed to connect to the database.");
                return CompletableFuture.completedFuture(false);
            }
        } catch (SQLException exception) {
            ReputationLogger.error("Failed to connect to database", exception);
            return CompletableFuture.completedFuture(false);
        }

        ReputationLogger.info("Connected to database with a pool size of " + poolSize + "!");
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> disconnect() {
        return CompletableFuture.supplyAsync(() -> {
            ReputationLogger.info("Disconnecting from database...");
            if (hikariDataSource != null) {
                hikariDataSource.close();
            }
            ReputationLogger.info("Disconnected from database!");
            return true;
        }, executorService);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isConnected() {
        return CompletableFuture.supplyAsync(() -> {
            if (hikariDataSource == null) {
                return false;
            }
            try(Connection connection = hikariDataSource.getConnection()) {
                return connection.isValid(5);
            } catch (SQLException exception) {
                return false;
            }
        }, executorService);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    @Override
    public ExecutorService getWorker() {
        return executorService;
    }
}
