package com.holysweet.questshop.integrations.ftbteams;

import com.holysweet.questshop.api.categories.CategorySetting;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.property.StringProperty;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.PlayerTeam;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class TeamCategories {
    public static final StringProperty CATEGORY_SETTINGS = new StringProperty(
            ResourceLocation.fromNamespaceAndPath("questshop", "category_settings"), ""
    );

    private TeamCategories() {}

    // Parse "ns:id=U,ns:other=L"
    private static Map<ResourceLocation, CategorySetting> parse(Team team) {
        String raw = team.getProperty(CATEGORY_SETTINGS);
        if (raw == null || raw.isBlank()) return Map.of();
        Map<ResourceLocation, CategorySetting> out = new LinkedHashMap<>();
        for (String tok : raw.split(",")) {
            tok = tok.trim();
            if (tok.isEmpty()) continue;
            int eq = tok.lastIndexOf('=');
            if (eq <= 0 || eq == tok.length() - 1) continue;
            ResourceLocation id = ResourceLocation.tryParse(tok.substring(0, eq));
            char c = tok.charAt(eq + 1);
            if (id != null) {
                if (c == 'U') out.put(id, CategorySetting.UNLOCKED);
                else if (c == 'L') out.put(id, CategorySetting.LOCKED);
            }
        }
        return out;
    }

    private static void write(Team team, Map<ResourceLocation, CategorySetting> map) {
        String s = map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .map(e -> e.getKey() + "=" + (e.getValue() == CategorySetting.UNLOCKED ? "U" : "L"))
                .collect(Collectors.joining(","));
        team.setProperty(CATEGORY_SETTINGS, s);
        team.markDirty();
    }

    public static CategorySetting getSetting(Team team, ResourceLocation id) {
        CategorySetting s = parse(team).get(id);
        return (s == null) ? CategorySetting.DEFAULT : s;
    }

    public static boolean setSetting(Team team, ResourceLocation id, CategorySetting setting) {
        Map<ResourceLocation, CategorySetting> map = new LinkedHashMap<>(parse(team));
        boolean changed;
        if (setting == CategorySetting.DEFAULT) {
            changed = (map.remove(id) != null);
        } else {
            CategorySetting prev = map.put(id, setting);
            changed = !Objects.equals(prev, setting);
        }
        if (changed) write(team, map);
        return changed;
    }

    // Merge unlocked progress only
    private static void mergeUnlockedInto(Team into, Team from) {
        if (into == null || from == null || into == from) return;
        Map<ResourceLocation, CategorySetting> A = new LinkedHashMap<>(parse(into));
        Map<ResourceLocation, CategorySetting> B = parse(from);
        boolean changed = false;
        for (var e : B.entrySet()) {
            if (e.getValue() == CategorySetting.UNLOCKED) {
                CategorySetting prev = A.put(e.getKey(), CategorySetting.UNLOCKED);
                if (prev != CategorySetting.UNLOCKED) changed = true;
            }
        }
        if (changed) write(into, A);
    }

    private static void PlayerJoinedPartyTeamEvent(@Nullable Team prev, @Nullable Team current) {
        if (prev instanceof PlayerTeam && current instanceof PartyTeam) {
            mergeUnlockedInto(current, prev);
        }
    }

    private static void PlayerLeftPartyTeamEvent(@Nullable Team prev, @Nullable Team current) {
        if (prev instanceof PartyTeam && current instanceof PlayerTeam) {
            mergeUnlockedInto(current, prev);
        }
    }

    public static void PlayerJoinedPartyTeamEvent(PlayerJoinedPartyTeamEvent e) {
        PlayerJoinedPartyTeamEvent(e.getPreviousTeam(), e.getTeam());
    }

    public static void PlayerLeftPartyTeamEvent(PlayerLeftPartyTeamEvent e) {
        PlayerLeftPartyTeamEvent(e.getTeam(), e.getPlayerTeam());
    }
}
