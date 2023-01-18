package com.orbitalstudios.minecraft;

import com.google.common.collect.ImmutableMap;
import com.orbitalstudios.minecraft.command.ReputationCommand;
import com.orbitalstudios.minecraft.extensions.*;
import com.orbitalstudios.minecraft.listener.ReputationHandler;
import com.orbitalstudios.minecraft.logger.ReputationLogger;
import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.repository.ReputationRepository;
import com.orbitalstudios.minecraft.storage.ReputationStorage;
import com.orbitalstudios.minecraft.storage.connector.ReputationStorageConnector;
import com.orbitalstudios.minecraft.storage.connector.impl.HikariStorageConnector;
import com.orbitalstudios.minecraft.storage.impl.SQLReputationStorage;
import com.orbitalstudios.minecraft.util.Colors;
import com.orbitalstudios.minecraft.vo.ReputationVO;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
public class ReputationPlugin extends JavaPlugin {

    @NotNull
    public static ReputationPlugin getInstance() {
        return getPlugin(ReputationPlugin.class);
    }

    private ReputationStorageConnector storageConnector;
    private ReputationStorage storage;

    private ReputationRepository reputationRepository;
    private ReputationVO reputationVO;

    private boolean isRunning = false;

    private Properties properties;

    private PlaceholderExpansion[] expansions;

    @Override
    public void onLoad() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();

        ReputationLogger.info("Loading configuration...");
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(
            new File(getDataFolder(), "config.yml")
        );

        ConfigurationSection settings = fileConfiguration.getConfigurationSection("Settings"),
            messages = fileConfiguration.getConfigurationSection("Messages");

        if (settings == null) {
            throw new NullPointerException("Settings is null");
        }

        ConfigurationSection permissions = settings.getConfigurationSection("Permissions");

        if (permissions == null) {
            throw new NullPointerException("Permissions is null");
        }

        if (messages == null) {
            throw new NullPointerException("Messages is null");
        }

        ImmutableMap<VoteType, Float> voteTypes = ImmutableMap.<VoteType, Float>builder()
            .put(VoteType.LIKE, (float) settings.getDouble("Like-Reputation-Change"))
            .put(VoteType.DISLIKE, (float) settings.getDouble("Dislike-Reputation-Change"))
            .build();

        ImmutableMap<VoteType, ChatColor> voteColors = ImmutableMap.<VoteType, ChatColor>builder()
            .put(VoteType.LIKE, Colors.getColor(settings.getString("Reputation-Color-Like")))
            .put(VoteType.DISLIKE, Colors.getColor(settings.getString("Reputation-Color-Dislike")))
            .build();

        Map<String, String> messagesMap = new LinkedHashMap<>();

        for (String key : messages.getKeys(false)) {
            messagesMap.put(key, ChatColor.translateAlternateColorCodes('&', messages.getString(key)));
        }

        ImmutableMap<String, String> permissionsMap = ImmutableMap.<String, String>builder()
            .put("Admin", permissions.getString("Admin"))
            .put("Like", permissions.getString("Like"))
            .put("Dislike", permissions.getString("Dislike"))
            .put("Others", permissions.getString("Others"))
            .put("See", permissions.getString("See"))
            .build();

        reputationVO = new ReputationVO(
            settings.getInt("Dislike-Cooldown", 60),
            voteTypes,
            permissionsMap,
            voteColors,
            messagesMap
        );

        saveResource("database.properties", false);

        properties = new Properties();

        File file = new File(getDataFolder(), "database.properties");
        if (!file.exists()) {
            ReputationLogger.error("Could not find database.properties file");
        }

        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            ReputationLogger.error("Error while loading database.properties", e);
        }
    }

    @Override
    public void onEnable() {
        ReputationLogger.info("Connecting to database...");
        storageConnector = new HikariStorageConnector(properties);

        storageConnector.connect()
            .thenAccept(success -> {
               if (!success) {
                   ReputationLogger.error("Could not connect to database");
                   Bukkit.getPluginManager().disablePlugin(this);
                   return;
               }

                ReputationLogger.info("Connected to database");
            });

        storage = new SQLReputationStorage(storageConnector);

//        storage = new DevelopmentReputationStorage();

        reputationRepository = new ReputationRepository();

        getServer().getCommandMap().register(
            "reputation",
            new ReputationCommand(
                storage,
                reputationRepository,
                reputationVO,
                this
            )
        );

        ReputationLogger.info("Registering listeners...");
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ReputationHandler(reputationRepository, storage, this), this);
        ReputationLogger.info("Listeners registered");

        isRunning = true;

        ReputationLogger.info("Registering placeholders...");
        if (!pluginManager.isPluginEnabled("PlaceholderAPI")) {
            ReputationLogger.error("PlaceholderAPI is not enabled");
            return;
        }

        expansions = new PlaceholderExpansion[] {
            new ColorExtension(reputationRepository, storage, reputationVO),
            new TotalExtension(reputationRepository, storage, reputationVO),
            new DislikeExtension(reputationRepository, storage),
            new LikeExtension(reputationRepository, storage),
            new PercentageExtension(reputationRepository, storage)
        };

        for (PlaceholderExpansion expansion : expansions) {
            if (!expansion.register()) {
                ReputationLogger.error("Could not register placeholder expansion " + expansion.getIdentifier());
            }
        }

        ReputationLogger.info("Registering placeholders... Done");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        if (isRunning) {
            isRunning = false;
        } else {
            return;
        }

        for (PlaceholderExpansion expansion : expansions) {
            expansion.unregister();
        }

        ReputationPlayer[] reputationPlayers = reputationRepository.getReputationPlayers()
            .values()
            .toArray(new ReputationPlayer[0]);

        ReputationLogger.info("Saving reputation data...");
        storage.batchPlayers(reputationPlayers)
            .thenAccept(success -> {
                if (!success) {
                    ReputationLogger.error("Could not save players to database");
                    return;
                }

                ReputationLogger.info("Saved players to database");
            });
    }

    public ReputationStorage getStorage() {
        return storage;
    }

    public ReputationRepository getReputationRepository() {
        return reputationRepository;
    }

    public ReputationStorageConnector getStorageConnector() {
        return storageConnector;
    }

    public ReputationVO getReputationVO() {
        return reputationVO;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
