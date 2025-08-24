// src/main/java/com/holysweet/questshop/integrations/ftbteams/FtbTeamsCoinsProvider.java
package com.holysweet.questshop.integrations.ftbteams;

import com.holysweet.questshop.api.coins.AccountRef;
import com.holysweet.questshop.api.coins.CoinsProvider;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class FTBTeamsCoinsProvider implements CoinsProvider {

    @Override
    public String id() {
        return "ftbteams";
    }

    @Override
    public AccountRef handleFor(ServerPlayer player) {
        Team team = teamForPlayerOrThrow(player);
        return new AccountRef.Team(team.getId());
    }

    @Override
    public int get(ServerLevel level, AccountRef handle) {
        Team team = normalizeToTeam(level, handle);
        return TeamCoins.get(team);
    }

    @Override
    public int set(ServerLevel level, AccountRef handle, int value) {
        Team team = normalizeToTeam(level, handle);
        int v = Math.max(0, value);
        TeamCoins.set(team, v);
        team.markDirty();
        return v;
    }

    @Override
    public int add(ServerLevel level, AccountRef handle, int delta) {
        Team team = normalizeToTeam(level, handle);
        TeamCoins.add(team, delta);
        team.markDirty();
        return TeamCoins.get(team);
    }

    // ---------- normalization helpers (always end up with a Team) ----------

    /** Resolve a concrete Team from any AccountRef. Throws if no team can be resolved. */
    private static Team normalizeToTeam(ServerLevel level, AccountRef handle) {
        return switch (handle) {
            case AccountRef.Team(UUID teamId)   -> teamByIdOrThrow(teamId);
            case AccountRef.Player(UUID player) -> teamForPlayerOrThrow(resolveOnline(level.getServer(), player));
        };
    }

    private static Team teamForPlayerOrThrow(ServerPlayer player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                .orElseThrow(() -> new IllegalStateException(
                        "FTB Teams: no team for online player " + player.getGameProfile().getName()));
    }

    private static Team teamByIdOrThrow(UUID id) {
        return FTBTeamsAPI.api().getManager().getTeamByID(id)
                .orElseThrow(() -> new IllegalStateException("FTB Teams: unknown team id " + id));
    }

    private static ServerPlayer resolveOnline(MinecraftServer server, UUID playerId) {
        ServerPlayer p = server.getPlayerList().getPlayer(playerId);
        if (p == null) {
            throw new IllegalStateException("Player " + playerId + " must be online for FTB Teams coin operations");
        }
        return p;
    }
}
