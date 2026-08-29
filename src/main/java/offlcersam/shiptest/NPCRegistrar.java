package offlcersam.shiptest;

import game.world.SectorGenerator;
import illuminatus.core.tools.util.Random;
import mods.ModLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NPCRegistrar {

    // tier (0-5) -> pool of custom ship base IDs
    private static final Map<Integer, List<Integer>> MOB_POOL = new HashMap<>();

    // sector tier (0-6+) -> pool of custom ship base IDs
    private static final Map<Integer, List<Integer>> BOSS_POOL = new HashMap<>();

    private static final ThreadLocal<Integer> STASHED_TIER = ThreadLocal.withInitial(() -> 0);

    private NPCRegistrar() { }

    public static void stashTier(int tier) {
        STASHED_TIER.set(tier);
    }

    public static int consumeStashedTier() {
        return STASHED_TIER.get();
    }

    /**
     * Makes a ship eligible to appear as a normal tiered NPC.
     * Weight is "tickets" relative to ONE vanilla-roll ticket for that tier.
     * Weight 1 makes it roughly as common as whichever single ship vanilla would have rolled.
     * Weight 2 makes it about twice as likely, etc.
     * Tune by testing in-game.
     */
    public static void registerTieredMob(int tier, int shipBaseId, int weight) {
        List<Integer> pool = MOB_POOL.computeIfAbsent(tier, t -> new ArrayList<>());
        for (int i = 0; i < Math.max(1, weight); i++) {
            pool.add(shipBaseId);
        }
        ModLogger.log("[ShipTest] Registered ship " + shipBaseId + " as tiered NPC (tier " + tier + ", weight " + weight + ")");
    }

    /**
     * Makes a ship eligible to appear as an elite/boss spawn for the given sector tier.
     * Same weighting rule as registerTieredMob.
     */
    public static void registerBoss(int sectorTier, int shipBaseId, int weight) {
        List<Integer> pool = BOSS_POOL.computeIfAbsent(sectorTier, t -> new ArrayList<>());
        for (int i = 0; i < Math.max(1, weight); i++) {
            pool.add(shipBaseId);
        }
        ModLogger.log("[ShipTest] Registered ship " + shipBaseId + " as boss spawn (sector tier " + sectorTier + ", weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after the vanilla tier switch in spawnTieredMob.
    public static int rollTieredMob(int tier, int vanillaShipId) {
        List<Integer> pool = MOB_POOL.get(tier);
        if (pool == null || pool.isEmpty()) {
            return vanillaShipId;
        }
        int totalTickets = 1 + pool.size();
        int roll = rng().nextInt(totalTickets);
        return roll == 0 ? vanillaShipId : pool.get(roll - 1);
    }

    // Called from SpawnNPCMixin right after the vanilla sector-tier switch in spawnBoss(Sector,int).
    public static int rollBoss(int sectorTier, int vanillaShipId) {
        List<Integer> pool = BOSS_POOL.get(sectorTier);
        if (pool == null || pool.isEmpty()) {
            return vanillaShipId;
        }
        int totalTickets = 1 + pool.size();
        int roll = rng().nextInt(totalTickets);
        return roll == 0 ? vanillaShipId : pool.get(roll - 1);
    }

    // Reuses the world's seeded RNG (same one vanilla spawn code uses) rather than a fresh
    // Random, so this doesn't affect world-gen determinism.
    private static Random rng() {
        if (SectorGenerator.rng == null) {
            SectorGenerator.rng = new Random(false);
            SectorGenerator.rng.setSeed(game.world.WorldGenerator.seed);
        }
        return SectorGenerator.rng;
    }
}