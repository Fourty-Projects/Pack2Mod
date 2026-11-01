package com.pack2mod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Mod Forge 1.20.1 - 47.4.18
 * Force le chargement de resource packs des le demarrage.
 *
 * <p>Ou mettre tes packs (cote SOURCE du mod) :
 * <pre>
 *   src/main/resources/forcedpacks/&lt;mon_pack&gt;/pack.mcmeta
 *   src/main/resources/forcedpacks/&lt;mon_pack&gt;/assets/...
 * </pre>
 * Chaque sous-dossier de {@code forcedpacks} contenant un {@code pack.mcmeta}
 * est charge comme un resource pack CLIENT, en {@code required=true},
 * position TOP, source BUILT_IN. Il est donc actif des le premier chargement
 * des ressources, sans action du joueur, et n'est pas desactivable.
 * Aucun dossier externe n'est lu : non modifiable en jeu.
 */
@Mod(Pack2Mod.MOD_ID)
public class Pack2Mod {
    public static final String MOD_ID = "pack2mod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Pack2Mod() {
        // L'enregistrement du listener se fait via
        // @Mod.EventBusSubscriber(bus = Bus.MOD) sur ForcedPacks :
        // AddPackFindersEvent est dispatche sur le bus MOD, pas le bus Forge.
        LOGGER.info("[Pack2Mod] Initialized. Forcing packs from forcedpacks/ (not editable in game)");
    }
}
