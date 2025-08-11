// src/main/java/com/holysweet/questshop/data/ShopReloader.java
package com.holysweet.questshop.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.util.*;
import java.util.function.Consumer;

import static com.holysweet.questshop.data.ShopCodecs.ENTRY_CODEC;

/** Datapack reload listener that fills {@link ShopCatalog}. */
public final class ShopReloader extends SimplePreparableReloadListener<ShopReloader.Parsed> implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String CATEGORIES_DIR = "questshop/shop_categories";
    private static final String ENTRIES_DIR    = "questshop/shop_entries";

    public static final ShopReloader INSTANCE = new ShopReloader();

    public record Parsed(Map<ResourceLocation, ShopCategory> categoriesById, List<ShopEntry> entries) {}

    private ShopReloader() {}

    /** Internal DTO for category JSON fields (ID comes from file path). */
    private static record CategoryFields(String display, boolean unlockedByDefault, int order) {}
    private static final com.mojang.serialization.MapCodec<CategoryFields> CATEGORY_FIELDS =
            com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(i -> i.group(
                    com.mojang.serialization.Codec.STRING.optionalFieldOf("display", "").forGetter(CategoryFields::display),
                    com.mojang.serialization.Codec.BOOL.optionalFieldOf("unlocked_by_default", true).forGetter(CategoryFields::unlockedByDefault),
                    com.mojang.serialization.Codec.INT.optionalFieldOf("order", 0).forGetter(CategoryFields::order)
            ).apply(i, CategoryFields::new));

    @Override
    protected Parsed prepare(ResourceManager rm, ProfilerFiller profiler) {
        Map<ResourceLocation, ShopCategory> categories = new LinkedHashMap<>();
        List<ShopEntry> entries = new ArrayList<>();

        // ---- categories: one JSON per file; ID derived from path
        rm.listResources(CATEGORIES_DIR, rl -> rl.getPath().endsWith(".json")).forEach((rl, res) -> {
            withJson(res, json -> {
                // Warn if authors left an "id" in the JSON; it is ignored.
                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();
                    if (obj.has("id")) {
                        ResourceLocation pathId = deriveId(rl, CATEGORIES_DIR);
                        LOGGER.warn("Category JSON {} contains a redundant \"id\"; ignored. Path-derived ID: {}", rl, pathId);
                    }
                }
                CATEGORY_FIELDS.codec().parse(JsonOps.INSTANCE, json).resultOrPartial(err ->
                        LOGGER.error("Category JSON error at {}: {}", rl, err)
                ).ifPresent(cf -> {
                    ResourceLocation id = deriveId(rl, CATEGORIES_DIR);
                    String display = !cf.display().isEmpty() ? cf.display() : id.getPath();
                    categories.put(id, new ShopCategory(id, display, cf.unlockedByDefault(), cf.order()));
                });
            });
        });

        // Ensure default "<modid>:general" exists
        ResourceLocation generalId = ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "general");
        categories.putIfAbsent(generalId, new ShopCategory(generalId, "General", true, 0));

        // ---- entries: allow array or single object per file
        rm.listResources(ENTRIES_DIR, rl -> rl.getPath().endsWith(".json")).forEach((rl, res) -> {
            withJson(res, json -> {
                if (json.isJsonArray()) {
                    json.getAsJsonArray().forEach(elem ->
                            ENTRY_CODEC.parse(JsonOps.INSTANCE, elem).resultOrPartial(err ->
                                    LOGGER.error("Entry JSON error at {}: {}", rl, err)
                            ).ifPresent(entries::add));
                } else {
                    ENTRY_CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(err ->
                            LOGGER.error("Entry JSON error at {}: {}", rl, err)
                    ).ifPresent(entries::add);
                }
            });
        });

        return new Parsed(categories, entries);
    }

    @Override
    protected void apply(Parsed parsed, ResourceManager rm, ProfilerFiller profiler) {
        ShopCatalog.INSTANCE.replace(parsed.categoriesById, parsed.entries);
        LOGGER.info("QuestShop datapack loaded: {} categories, {} entries",
                parsed.categoriesById.size(), parsed.entries.size());
    }

    // ------------ helpers ------------

    private static void withJson(Resource res, Consumer<JsonElement> consumer) {
        try (BufferedReader r = res.openAsReader()) {
            JsonElement json = JsonParser.parseReader(r);
            consumer.accept(json);
        } catch (Exception ex) {
            LOGGER.error("Failed reading {}", res.sourcePackId(), ex);
        }
    }

    private static ResourceLocation deriveId(ResourceLocation file, String baseDir) {
        // data/<ns>/<baseDir>/<path>.json => <ns>:<path>
        String path = file.getPath();
        int idx = path.indexOf(baseDir);
        String rel = idx >= 0 ? path.substring(idx + baseDir.length()) : path;
        if (rel.startsWith("/")) rel = rel.substring(1);
        if (rel.endsWith(".json")) rel = rel.substring(0, rel.length() - 5);
        return ResourceLocation.fromNamespaceAndPath(file.getNamespace(), rel);
    }
}
