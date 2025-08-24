package com.holysweet.questshop.api.coins;

import java.util.UUID;

public sealed interface AccountRef
        permits AccountRef.Player, AccountRef.Team {

    record Player(UUID playerId) implements AccountRef {}
    record Team(UUID teamId)     implements AccountRef {}
}