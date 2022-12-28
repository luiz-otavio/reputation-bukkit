package com.orbitalstudios.minecraft.storage.connector;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
public interface ReputationStorageConnector {

    @NotNull
    CompletableFuture<Boolean> connect();

    @NotNull
    CompletableFuture<Boolean> disconnect();

    @NotNull
    CompletableFuture<Boolean> isConnected();

    Connection getConnection() throws SQLException;

    ExecutorService getWorker();


}
