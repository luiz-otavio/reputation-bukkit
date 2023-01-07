package com.orbitalstudios.minecraft.command;

import com.orbitalstudios.minecraft.ReputationPlugin;
import com.orbitalstudios.minecraft.logger.ReputationLogger;
import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.repository.ReputationRepository;
import com.orbitalstudios.minecraft.storage.ReputationStorage;
import com.orbitalstudios.minecraft.util.Synchronize;
import com.orbitalstudios.minecraft.vo.ReputationVO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
public class ReputationCommand extends Command {

    private final ReputationPlugin reputationPlugin;

    private final ReputationStorage reputationStorage;
    private final ReputationRepository reputationRepository;

    private final ReputationVO reputationVO;

    public ReputationCommand(
        @NotNull ReputationStorage reputationStorage,
        @NotNull ReputationRepository reputationRepository,
        @NotNull ReputationVO reputationVO,
        @NotNull ReputationPlugin reputationPlugin
    ) {
        super("reputation", "Reputation command for Chiko.", "/reputation", List.of("rep"));

        this.reputationStorage = reputationStorage;
        this.reputationRepository = reputationRepository;
        this.reputationVO = reputationVO;
        this.reputationPlugin = reputationPlugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length > 0) {
            if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.isOp() || !sender.hasPermission(reputationVO.adminPermission())) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                    return true;
                }

                reputationPlugin.reloadConfig();

                reputationPlugin.onDisable();
                reputationPlugin.onLoad();
                reputationPlugin.onEnable();

                sender.sendMessage(ChatColor.GREEN + "Reputation reloaded.");
                return true;
            }

            if (args[0].equalsIgnoreCase("set")) {
                if (!sender.hasPermission(reputationVO.adminPermission()) && !sender.isOp()) {
                    sender.sendMessage(
                        reputationVO.getMessage("No-Permission")
                    );

                    return true;
                }

                if (args.length != 4) {
                    sender.sendMessage(
                        reputationVO.getMessage("Invalid-Arguments")
                    );

                    return true;
                }

                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(
                        reputationVO.getMessage("Invalid-Arguments")
                    );

                    return true;
                }

                if (amount < 0) {
                    sender.sendMessage(
                        reputationVO.getMessage("Invalid-Arguments")
                    );

                    return true;
                }

                ReputationPlayer reputationPlayer = reputationRepository.getReputationPlayer(args[1]);
                if (reputationPlayer == null) {
                    reputationPlayer = reputationStorage.retrievePlayer(args[1])
                        .join();

                    if (reputationPlayer == null) {
                        sender.sendMessage(
                            reputationVO.getMessage("No-Player-Found")
                        );

                        return true;
                    }
                }

                VoteType targetType = null;
                for (VoteType type : VoteType.values()) {
                    if (type.name().equalsIgnoreCase(args[2])) {
                        targetType = type;
                        break;
                    }
                }

                if (targetType == null) {
                    sender.sendMessage(
                        reputationVO.getMessage("Invalid-Vote-Type")
                    );

                    return true;
                }

                switch (targetType) {
                    case LIKE -> reputationPlayer.setVotes(VoteType.LIKE, amount);
                    case DISLIKE -> reputationPlayer.setVotes(VoteType.DISLIKE, amount);
                }

                reputationStorage.updateVotes(
                    reputationPlayer,
                    targetType,
                    amount
                ).thenAccept(success -> {
                    ReputationLogger.info("Player reputation has been set to " + amount + " by " + sender.getName() + ".");
                    sender.sendMessage(
                        ChatColor.GREEN + "Player reputation has been set to " + amount + ". in database."
                    );
                });

                sender.sendMessage(
                    reputationVO.getMessage(
                        "Player-Reputation",
                        "%player%", reputationPlayer.getName(),
                        "%reputation%", reputationPlayer.getReputation(reputationVO),
                        "%likes%", reputationPlayer.getVotes(VoteType.LIKE),
                        "%dislikes%", reputationPlayer.getVotes(VoteType.DISLIKE)
                    )
                );

                return true;
            }

            ReputationPlayer reputationPlayer = reputationRepository.getReputationPlayer(args[0]);
            if (reputationPlayer == null) {
                reputationPlayer = reputationStorage.retrievePlayer(args[0])
                    .join();

                if (reputationPlayer == null) {
                    sender.sendMessage(
                        reputationVO.getMessage("No-Player-Found")
                    );

                    return true;
                }
            }

            if (args.length == 2) {
                VoteType targetType = null;
                for (VoteType type : VoteType.values()) {
                    if (type.name().equalsIgnoreCase(args[1])) {
                        targetType = type;
                        break;
                    }
                }

                if (targetType == null) {
                    sender.sendMessage(
                        reputationVO.getMessage("Invalid-Vote-Type")
                    );

                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(
                        reputationVO.getMessage("Invalid-Arguments")
                    );
                    return true;
                }

                ReputationPlayer author = reputationRepository.getReputationPlayer(player.getUniqueId());
                if (author == null) {
                    player.sendMessage(
                        reputationVO.getMessage("No-Player-Found")
                    );

                    return true;
                }

                if (author.getUniqueId().equals(reputationPlayer.getUniqueId())) {
                    player.sendMessage(
                        reputationVO.getMessage("Cannot-Vote-Yourself")
                    );

                    return true;
                }

                ReputationPlayer finalAuthor = author,
                    finalReputationPlayer = reputationPlayer;

                VoteType finalTargetType = targetType;
                reputationStorage.hasVoted(author, reputationPlayer, reputationVO.dislikeCooldown())
                    .thenAccept(hasVoted -> {
                        if (hasVoted) {
                            player.sendMessage(
                                reputationVO.getMessage(
                                    "Dislike-Cooldown",
                                    "%cooldown%", reputationVO.dislikeCooldown()
                                )
                            );

                            return;
                        }

                        switch (finalTargetType) {
                            case LIKE -> {
                                Synchronize.callToSync(() -> {
                                    finalReputationPlayer.computeVote(VoteType.LIKE);
                                });

                                finalAuthor.setHistory(VoteType.LIKE, Instant.now());

                                player.sendMessage(
                                    reputationVO.getMessage(
                                        "Like-Message",
                                        "%player%", finalReputationPlayer.getName()
                                    )
                                );

                                reputationStorage.computeVote(finalAuthor, finalReputationPlayer, VoteType.LIKE)
                                    .thenAccept(unused -> {
                                        ReputationLogger.info("Vote registered: %s -> %s", finalAuthor.getName(), finalReputationPlayer.getName());
                                    });

                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                            }
                            case DISLIKE -> {
                                Synchronize.callToSync(() -> {
                                    finalReputationPlayer.computeVote(VoteType.DISLIKE);
                                });

                                finalAuthor.setHistory(VoteType.DISLIKE, Instant.now());

                                player.sendMessage(
                                    reputationVO.getMessage(
                                        "Dislike-Message",
                                        "%player%", finalReputationPlayer.getName()
                                    )
                                );

                                reputationStorage.computeVote(finalAuthor, finalReputationPlayer, VoteType.DISLIKE)
                                    .thenAccept(unused -> {
                                        ReputationLogger.info("Vote registered: %s -> %s", finalAuthor.getName(), finalReputationPlayer.getName());
                                    });

                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                            }
                        }
                    });

                return true;
            }

            sender.sendMessage(
                reputationVO.getMessage(
                    "Player-Reputation",
                    "%player%", reputationPlayer.getName(),
                    "%reputation%", reputationPlayer.getReputation(reputationVO),
                    "%likes%", reputationPlayer.getVotes(VoteType.LIKE),
                    "%dislikes%", reputationPlayer.getVotes(VoteType.DISLIKE)
                )
            );

            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                reputationVO.getMessage("Invalid-Arguments")
            );
            return true;
        }

        ReputationPlayer reputationPlayer = reputationRepository.getReputationPlayer(player.getUniqueId());
        if (reputationPlayer == null) {
            ReputationLogger.error("Reputation player not found");
            player.sendMessage(
                reputationVO.getMessage("Something-Went-Wrong")
            );

            return true;
        }

        float reputation = reputationPlayer.getReputation(reputationVO);

        player.sendMessage(
            reputationVO.getMessage(
                "Reputation-Status",
                "%reputation%", reputation,
                "%likes%", reputationPlayer.getVotes(VoteType.LIKE),
                "%dislikes%", reputationPlayer.getVotes(VoteType.DISLIKE)
            )
        );

        if (reputation < 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1, 1);
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1 && args[0].isEmpty()) {
            List<String> range = new ArrayList<>(Bukkit.getOnlinePlayers().size() + 1);

            range.add("set");

            for (Player player : Bukkit.getOnlinePlayers()) {
                range.add(player.getName());
            }

            return range;
        }

        if (args[0].equalsIgnoreCase("set") && sender.isOp()) {
            return switch (args.length) {
                case 2 -> Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
                case 3 -> Arrays.stream(VoteType.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());
                default -> Collections.emptyList();
            };
        } else {
            if (Bukkit.getPlayerExact(args[0]) == null) {
                return Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            }

            return Arrays.stream(VoteType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        }
    }
}
