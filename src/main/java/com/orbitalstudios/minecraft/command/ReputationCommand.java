package com.orbitalstudios.minecraft.command;

import com.orbitalstudios.minecraft.ReputationPlugin;
import com.orbitalstudios.minecraft.logger.ReputationLogger;
import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.repository.ReputationRepository;
import com.orbitalstudios.minecraft.storage.ReputationStorage;
import com.orbitalstudios.minecraft.util.Formatter;
import com.orbitalstudios.minecraft.util.Synchronize;
import com.orbitalstudios.minecraft.vo.ReputationVO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoField;
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
                if (!sender.isOp() && !reputationVO.hasPermission(sender, "Admin")) {
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
                if (!reputationVO.hasPermission(sender, "Admin") && !sender.isOp()) {
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
                    } else {
                        reputationRepository.putReputationPlayer(reputationPlayer);
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
                        reputationVO.getMessage(
                            "Reputation-Updated",
                            "%amount%", amount,
                            "%player%", args[1]
                        )
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
                } else {
                    reputationRepository.putReputationPlayer(reputationPlayer);
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

                String permission = switch (targetType) {
                    case DISLIKE -> "Dislike";
                    case LIKE -> "Like";
                };

                if (!reputationVO.hasPermission(sender, permission) && !sender.isOp()) {
                    sender.sendMessage(
                        reputationVO.getMessage("No-Permission")
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
                    .thenAccept(instant -> {
                        if (instant != null) {
                            long diff = System.currentTimeMillis() - instant.toEpochMilli(),
                                toFormat = reputationVO.dislikeCooldown() * 1000 - diff;

                            player.sendMessage(
                                reputationVO.getMessage(
                                    "Dislike-Cooldown",
                                    "%cooldown%", Formatter.format(toFormat)
                                )
                            );

                            return;
                        }

                        switch (finalTargetType) {
                            case LIKE -> {
                                player.sendMessage(
                                    reputationVO.getMessage(
                                        "Like-Message",
                                        "%player%", finalReputationPlayer.getName()
                                    )
                                );

                                reputationStorage.computeVote(finalAuthor, finalReputationPlayer, VoteType.LIKE)
                                    .thenAccept(success -> {
                                        if (!success) {
                                            ReputationLogger.warn("Failed to compute vote for " + finalReputationPlayer.getName() + ".");
                                            return;
                                        }

                                        ReputationLogger.info("Vote registered: %s -> %s", finalAuthor.getName(), finalReputationPlayer.getName());

                                        Synchronize.callToSync(() -> {
                                            finalReputationPlayer.computeVote(VoteType.LIKE);
                                        });

                                        finalAuthor.setHistory(VoteType.LIKE, Instant.now());
                                    });
                            }
                            case DISLIKE -> {
                                player.sendMessage(
                                    reputationVO.getMessage(
                                        "Dislike-Message",
                                        "%player%", finalReputationPlayer.getName()
                                    )
                                );

                                reputationStorage.computeVote(finalAuthor, finalReputationPlayer, VoteType.DISLIKE)
                                    .thenAccept(success -> {
                                        if (!success) {
                                            ReputationLogger.warn("Vote not registered: %s -> %s", finalAuthor.getName(), finalReputationPlayer.getName());
                                            return;
                                        }

                                        ReputationLogger.info("Vote registered: %s -> %s", finalAuthor.getName(), finalReputationPlayer.getName());

                                        Synchronize.callToSync(() -> {
                                            finalReputationPlayer.computeVote(VoteType.DISLIKE);
                                        });

                                        finalAuthor.setHistory(VoteType.DISLIKE, Instant.now());
                                    });
                            }
                        }
                    });

                return true;
            }

            if (!reputationVO.hasPermission(sender, "Others") && !sender.isOp()) {
                sender.sendMessage(
                    reputationVO.getMessage("No-Permission")
                );

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

        if (!reputationVO.hasPermission(sender, "See") && !sender.isOp()) {
            sender.sendMessage(
                reputationVO.getMessage("No-Permission")
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

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1 && args[0].isEmpty()) {
            List<String> range = new ArrayList<>(Bukkit.getOnlinePlayers().size() + 1);

            if (reputationVO.hasPermission(sender, "Admin") || sender.isOp()) {
                range.add("reload");
                range.add("set");
            }

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
