package com.orbitalstudios.minecraft.command;

import com.orbitalstudios.minecraft.logger.ReputationLogger;
import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import com.orbitalstudios.minecraft.repository.ReputationRepository;
import com.orbitalstudios.minecraft.storage.ReputationStorage;
import com.orbitalstudios.minecraft.vo.ReputationVO;
import lombok.RequiredArgsConstructor;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.annotation.Optional;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
@RequiredArgsConstructor
public class ReputationCommand {

    private final ReputationStorage reputationStorage;
    private final ReputationRepository reputationRepository;

    private final ReputationVO reputationVO;

    @Command(
        name = "reputation",
        target = CommandTarget.PLAYER
    )
    public void handleReputationCommand(Context<Player> context, @Optional String target, String voteType) {
        Player player = context.getSender();

        if (target != null) {
            ReputationPlayer reputationPlayer = reputationRepository.getReputationPlayer(target);
            if (reputationPlayer == null) {
                reputationPlayer = reputationStorage.retrievePlayer(target)
                    .join();

                if (reputationPlayer == null) {
                    player.sendMessage(
                        reputationVO.getMessage("No-Player-Found")
                    );

                    return;
                }
            }

            if (voteType != null) {
                VoteType targetType = null;
                for (VoteType type : VoteType.values()) {
                    if (type.name().equalsIgnoreCase(voteType)) {
                        targetType = type;
                        break;
                    }
                }

                if (targetType == null) {
                    player.sendMessage(
                        reputationVO.getMessage("Invalid-Vote-Type")
                    );

                    return;
                }

                ReputationPlayer author = reputationRepository.getReputationPlayer(player.getUniqueId());
                if (author == null) {
                    player.sendMessage(
                        reputationVO.getMessage("No-Player-Found")
                    );

                    return;
                }

                final ReputationPlayer finalAuthor = author,
                    finalReputationPlayer = reputationPlayer;
                switch (targetType) {
                    case LIKE -> {
                        reputationPlayer.computeVote(VoteType.LIKE);

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
                        reputationStorage.hasVoted(finalAuthor, reputationVO.dislikeCooldown())
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

                                finalReputationPlayer.computeVote(VoteType.DISLIKE);

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
                            });
                    }
                }
            }

            player.sendMessage(
                reputationVO.getMessage(
                    "Player-Reputation",
                    "%player%", reputationPlayer.getName(),
                    "%reputation%", reputationPlayer.getReputation(reputationVO),
                    "%likes%", reputationPlayer.getVotes(VoteType.LIKE),
                    "%dislikes%", reputationPlayer.getVotes(VoteType.DISLIKE)
                )
            );

            return;
        }

        ReputationPlayer reputationPlayer = reputationRepository.getReputationPlayer(player.getUniqueId());
        if (reputationPlayer == null) {
            ReputationLogger.error("Reputation player not found");
            player.sendMessage(
                reputationVO.getMessage("Something-Went-Wrong")
            );

            return;
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
    }

    @Command(
        name = "reputation.set"
    )
    public void handleReputationSetCommand(Context<Player> context, @Optional String target, @Optional String voteType, @Optional(def = "-1") int amount) {
        Player player = context.getSender();
        if (!player.hasPermission(reputationVO.adminPermission())) {
            player.sendMessage(
                reputationVO.getMessage("No-Permission")
            );

            return;
        }

        if (target == null) {
            player.sendMessage(
                reputationVO.getMessage("Invalid-Arguments")
            );

            return;
        }

        if (voteType == null) {
            player.sendMessage(
                reputationVO.getMessage("Invalid-Arguments")
            );

            return;
        }

        if (amount < 0) {
            player.sendMessage(
                reputationVO.getMessage("Invalid-Arguments")
            );

            return;
        }


        ReputationPlayer reputationPlayer = reputationRepository.getReputationPlayer(target);
        if (reputationPlayer == null) {
            reputationPlayer = reputationStorage.retrievePlayer(target)
                .join();

            if (reputationPlayer == null) {
                player.sendMessage(
                    reputationVO.getMessage("No-Player-Found")
                );

                return;
            }
        }

        VoteType targetType = null;
        for (VoteType type : VoteType.values()) {
            if (type.name().equalsIgnoreCase(voteType)) {
                targetType = type;
                break;
            }
        }

        if (targetType == null) {
            player.sendMessage(
                reputationVO.getMessage("Invalid-Vote-Type")
            );

            return;
        }

        switch (targetType) {
            case LIKE -> reputationPlayer.setVotes(VoteType.LIKE, amount);
            case DISLIKE -> reputationPlayer.setVotes(VoteType.DISLIKE, amount);
        }

        player.sendMessage(
            reputationVO.getMessage(
                "Player-Reputation",
                "%player%", reputationPlayer.getName(),
                "%reputation%", reputationPlayer.getReputation(reputationVO),
                "%likes%", reputationPlayer.getVotes(VoteType.LIKE),
                "%dislikes%", reputationPlayer.getVotes(VoteType.DISLIKE)
            )
        );
    }
}
