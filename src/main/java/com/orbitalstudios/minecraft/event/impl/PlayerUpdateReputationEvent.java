package com.orbitalstudios.minecraft.event.impl;

import com.orbitalstudios.minecraft.event.ReputationEvent;
import com.orbitalstudios.minecraft.pojo.ReputationPlayer;
import com.orbitalstudios.minecraft.pojo.vote.VoteType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Luiz O. F. Corrêa
 * @since 27/12/2022
 **/
@AllArgsConstructor
@Getter
public class PlayerUpdateReputationEvent extends ReputationEvent {

    private final ReputationPlayer target, source;

    private final VoteType voteType;

    private int reputation;

}
