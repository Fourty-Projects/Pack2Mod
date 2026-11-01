package com.pack2mod;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Enregistre les packs forces comme packs CLIENT "required" (toujours actifs).
 * Appele des le demarrage du jeu, y compris au tout premier chargement des ressources :
 * aucune action joueur, aucun rechargement manuel requis.
 *
 * <p>IMPORTANT : cet evenement est dispatche par Forge sur le bus MOD
 * (via ModLoader.postEvent dans ClientModLoader), PAS sur le bus Forge.
 * D'ou l'enregistrement via {@code @Mod.EventBusSubscriber(bus = Bus.MOD)}.
 */
@Mod.EventBusSubscriber(modid = Pack2Mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForcedPacks {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ForcedPacks() {}

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        // On ne force que les ressources client (textures, modeles, sons, lang...).
        // Les datapacks serveur ne sont pas concernes.
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }

        List<Pack> forced = new ArrayList<>();

        // Packs embarques dans le JAR du mod : src/main/resources/forcedpacks/<pack>/
        // Volontairement aucune lecture de dossier externe (config/, resourcepacks/, etc.)
        // pour que ce soit non modifiable en jeu : seul le contenu du jar est force.
        try {
            Path builtinRoot = ModList.get()
                    .getModFileById(Pack2Mod.MOD_ID)
                    .getFile()
                    .findResource("forcedpacks");
            collectFolderPacks(builtinRoot, forced);
        } catch (Exception e) {
            LOGGER.warn("[Pack2Mod] Could not scan forcedpacks/ inside the jar: {}", e.toString());
        }

        if (forced.isEmpty()) {
            LOGGER.info("[Pack2Mod] No forced pack found in forcedpacks/.");
            return;
        }

        for (Pack pack : forced) {
            LOGGER.info("[Pack2Mod] Forcing pack: {}", pack.getId());
        }

        // required=true + Position.TOP + fixed=true => actif des le demarrage,
        // prioritaire, non desactivable depuis l'ecran des packs.
        event.addRepositorySource(packConsumer -> {
            for (Pack pack : forced) {
                packConsumer.accept(pack);
            }
        });
    }

    /**
     * Scanne le dossier embarque : chaque sous-dossier contenant un pack.mcmeta
     * devient un Pack force.
     */
    private static void collectFolderPacks(Path root, List<Pack> out) {
        if (root == null || !Files.isDirectory(root)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path packDir : stream) {
                if (!Files.isDirectory(packDir)) {
                    continue;
                }
                Path mcmeta = packDir.resolve("pack.mcmeta");
                if (!Files.isRegularFile(mcmeta)) {
                    LOGGER.debug("[Pack2Mod] Skipped (no pack.mcmeta): {}", packDir);
                    continue;
                }
                String folderName = packDir.getFileName().toString();
                String safe = folderName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
                String id = "builtin/pack2mod_" + safe;
                Component title = Component.literal("Pack2Mod: " + folderName);

                try {
                    Pack.ResourcesSupplier supplier = packId -> new PathPackResources(packId, packDir, true);
                    // Signature 1.20.1 : (id, titre, required, resources, type, position, source).
                    // required=true => toujours actif des le demarrage, masque de l'ecran des packs.
                    Pack pack = Pack.readMetaAndCreate(
                            id,
                            title,
                            true,
                            supplier,
                            PackType.CLIENT_RESOURCES,
                            Pack.Position.TOP,
                            PackSource.BUILT_IN
                    );
                    if (pack != null) {
                        out.add(pack);
                    } else {
                        LOGGER.warn("[Pack2Mod] Unreadable pack.mcmeta, pack skipped: {}", packDir);
                    }
                } catch (Exception e) {
                    LOGGER.warn("[Pack2Mod] Pack skipped (read error) {}: {}", packDir, e.toString());
                }
            }
        } catch (IOException e) {
            LOGGER.warn("[Pack2Mod] Scan error {}: {}", root, e.toString());
        }
    }
}
