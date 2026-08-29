package offlcersam.shiptest;

import _database.NameDatabase;
import game.world.SectorGenerator;
import game.objects.SpaceShip;
import offlcersam.shiptest.mixin.SpawnNPCAccessor;
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

    // Vanilla candidate count per tier, counted directly from the rngSelection() lists in
    // _database.SpawnNPC.spawnTieredMob's tier switch.
    // tier: 0, 1, 2, 3, 4, 5
    private static final int[] VANILLA_MOB_POOL_SIZE   = { 19, 19, 14, 12, 8, 8 };

    // Vanilla candidate count per sector tier, counted from spawnBoss(Sector,int) switch case.
    // sector tier: 0, 1, 2, 3, 4, 5, 6+
    private static final int[] VANILLA_BOSS_POOL_SIZE  = { 5, 6, 8, 15, 15, 13, 15 };

    // Pool of custom ship base IDs eligible to replace vanilla police spawns (not tiered).
    private static final List<Integer> POLICE_POOL = new ArrayList<>();

    // Vanilla ticket count for spawnPolice(Sector,int): rngVal(0,4) has 5 equally-likely
    // outcomes (1 ticket rolls ship 19, the other 4 roll ship 90).
    private static final int VANILLA_POLICE_POOL_SIZE = 5;

    // Vanilla ticket count for spawnTempPoliceMob(...): rngSelection(20,20,20,19) is a
    // 4-entry pool (3 tickets ship 20, 1 ticket ship 19). Used for temp/escort police groups.
    private static final int VANILLA_TEMP_POLICE_POOL_SIZE = 4;

    // bucket 0-4, where 4 means "tier 4 or higher" - matches Utils.constrain(0,tier,5) collapsing
    // tiers 4 and 5 into the same default case in both spawnRogueDrones and spawnTempRogueDrones.
    private static final Map<Integer, List<Integer>> ROGUE_DRONE_POOL = new HashMap<>();

    // Custom ship base ID -> the gear preset it should use, since vanilla's configRogueDrone(...)
    // keys entirely off the literal ship id and falls back to weak tier-0 gear for anything else.
    private static final Map<Integer, RogueDroneGear> ROGUE_DRONE_GEAR = new HashMap<>();

    // Range sizes (highestShipIndex - lowestShipIndex + 1) per bucket, counted from both
    // spawnRogueDrones and spawnTempRogueDrones (they use identical range tables).
    // bucket:                              0  1  2  3  4+
    private static final int[] VANILLA_ROGUE_DRONE_POOL_SIZE = { 4, 4, 5, 5, 5 };

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
        int vanillaTickets = VANILLA_MOB_POOL_SIZE[Math.max(0, Math.min(tier, VANILLA_MOB_POOL_SIZE.length - 1))];
        int totalTickets = vanillaTickets + pool.size();
        int roll = rng().nextInt(totalTickets);
        return roll < vanillaTickets ? vanillaShipId : pool.get(roll - vanillaTickets);
    }

    // Called from SpawnNPCMixin right after the vanilla sector-tier switch in spawnBoss(Sector,int).
    public static int rollBoss(int sectorTier, int vanillaShipId) {
        List<Integer> pool = BOSS_POOL.get(sectorTier);
        if (pool == null || pool.isEmpty()) {
            return vanillaShipId;
        }
        int vanillaTickets = VANILLA_BOSS_POOL_SIZE[Math.max(0, Math.min(sectorTier, VANILLA_BOSS_POOL_SIZE.length - 1))];
        int totalTickets = vanillaTickets + pool.size();
        int roll = rng().nextInt(totalTickets);
        return roll < vanillaTickets ? vanillaShipId : pool.get(roll - vanillaTickets);
    }

    /**
     * Makes a ship eligible to appear as police spawn. (Both single spawnPolice roll and the grouped spawnTempPoliceMob roll share this pool).
     * Not tiered, as police spawns are not tier-gated, but same weighting mechanic as the registerTieredMob/registerBoss.
     */
    public static void registerPolice(int shipBaseId, int weight) {
        for (int i = 0; i < Math.max(1, weight); i++) {
            POLICE_POOL.add(shipBaseId);
        }
        ModLogger.log("[ShipTest] Registered ship " + shipBaseId + " as police spawn (weight " + weight + ")");
    }

    // Called from SpawnNPCMixin right after spawnPolice's switch converges on a chosen ship id.
    public static int rollPolice(int vanillaShipId) {
        if (POLICE_POOL.isEmpty()) {
            return vanillaShipId;
        }
        int totalTickets = VANILLA_POLICE_POOL_SIZE + POLICE_POOL.size();
        int roll = rng().nextInt(totalTickets);
        return roll < VANILLA_POLICE_POOL_SIZE ? vanillaShipId : POLICE_POOL.get(roll - VANILLA_POLICE_POOL_SIZE);
    }

    // Called from SpawnNPCMixin right after spawnTempPoliceMob's per-iteration roll.
    public static int rollTempPolice(int vanillaShipId) {
        if (POLICE_POOL.isEmpty()) {
            return vanillaShipId;
        }
        int totalTickets = VANILLA_TEMP_POLICE_POOL_SIZE + POLICE_POOL.size();
        int roll = rng().nextInt(totalTickets);
        return roll < VANILLA_TEMP_POLICE_POOL_SIZE ? vanillaShipId : POLICE_POOL.get(roll - VANILLA_TEMP_POLICE_POOL_SIZE);
    }

    // Mirrors the fields configRogueDrone() assigns per vanilla ship id.
    // LevelMin/levelMax feed classSkill.set(), creditMin/creditMax feed cargo.setCurrency().
    public record RogueDroneGear(
            int tier,
            int weaponLaser,
            int weaponBay,
            int energyFullID,
            int levelMin,
            int levelMax,
            long creditMin,
            long creditMax
    ) { }

    // Presets from configRogueDrone's 9 cases (shipId 181-189), for reference.
    // e.g. NPCRegistrar.registerRogueDrone(2, myShipId, 1, NPCRegistrar.ROGUE_GEAR_TIER2_B).
    public static final RogueDroneGear ROGUE_GEAR_TIER0    = new RogueDroneGear(0, 302610000, 302610000, 709010000, 1, 2, 200, 400);
    public static final RogueDroneGear ROGUE_GEAR_TIER1_A  = new RogueDroneGear(1, 302620000, 306170000, 709020000, 2, 4, 400, 600);
    public static final RogueDroneGear ROGUE_GEAR_TIER1_B  = new RogueDroneGear(1, 302620000, 306170000, 709020000, 4, 6, 600, 800);
    public static final RogueDroneGear ROGUE_GEAR_TIER2_A  = new RogueDroneGear(2, 302630000, 306180000, 709030000, 7, 9, 800, 1000);
    public static final RogueDroneGear ROGUE_GEAR_TIER2_B  = new RogueDroneGear(2, 302630000, 306180000, 709030000, 10, 12, 1000, 1500);
    public static final RogueDroneGear ROGUE_GEAR_TIER2_C  = new RogueDroneGear(2, 302640000, 306180000, 709040000, 13, 15, 1500, 3000);
    public static final RogueDroneGear ROGUE_GEAR_TIER3_A  = new RogueDroneGear(3, 302640000, 306190000, 709040000, 16, 18, 3000, 6000);
    public static final RogueDroneGear ROGUE_GEAR_TIER3_B  = new RogueDroneGear(3, 302640000, 306190000, 709050000, 20, 23, 6000, 9000);
    public static final RogueDroneGear ROGUE_GEAR_TIER3_C  = new RogueDroneGear(3, 302640000, 306190000, 709050000, 25, 28, 9000, 12000);

    /**
     * Makes a ship eligible to spawn as a rogue drone in the given tier (0-4, where 4 covers tier 4+).
     * Gear controls its loadout, credit drop, and level, since without an override it would otherwise fall into vanilla's tier-0 default gear (see class comment on RogueDroneGear).
     */
    public static void registerRogueDrone(int tier, int shipBaseId, int weight, RogueDroneGear gear) {
        int bucket = rogueDroneBucket(tier);
        List<Integer> pool = ROGUE_DRONE_POOL.computeIfAbsent(bucket, b -> new ArrayList<>());
        for (int i = 0; i < Math.max(1, weight); i++) {
            pool.add(shipBaseId);
        }
        ROGUE_DRONE_GEAR.put(shipBaseId, gear);
        ModLogger.log("[ShipTest] Registered ship " + shipBaseId + " as rogue drone (tier " + tier + " -> bucket " + bucket + ", weight " + weight + ")");
    }

    // Maps a tier value to the 0-4 bucket vanilla's Utils.constrain(0,tier,5) switch uses.
    public static int rogueDroneBucket(int tier) {
        int clamped = Math.max(0, Math.min(tier, 5));
        return Math.min(clamped, 4);
    }

    // Called from SpawnNPCMixin right after the vanilla range roll in spawnRogueDrones/spawnTempRogueDrones.
    public static int rollRogueDrone(int bucket, int vanillaShipId) {
        List<Integer> pool = ROGUE_DRONE_POOL.get(bucket);
        if (pool == null || pool.isEmpty()) {
            return vanillaShipId;
        }
        int vanillaTickets = VANILLA_ROGUE_DRONE_POOL_SIZE[Math.max(0, Math.min(bucket, VANILLA_ROGUE_DRONE_POOL_SIZE.length - 1))];
        int totalTickets = vanillaTickets + pool.size();
        int roll = rng().nextInt(totalTickets);
        return roll < vanillaTickets ? vanillaShipId : pool.get(roll - vanillaTickets);
    }

    public static boolean isCustomRogueDrone(int shipId) {
        return ROGUE_DRONE_GEAR.containsKey(shipId);
    }

    // Called from SpawnNPCMixin instead of vanilla configRogueDrone() when shipId is one of ours,
    // reproduces configRogueDrone's tail exactly with our configured gear.
    public static void configureCustomRogueDrone(int shipId, SpaceShip tempShip) {
        RogueDroneGear gear = ROGUE_DRONE_GEAR.get(shipId);
        if (gear == null) {
            return;
        }
        SpawnNPCAccessor.invokePopulateShipGear(tempShip, gear.tier(), 6, false);
        tempShip.hull.energySlots.removeAll();
        tempShip.hull.equipEnergy(gear.energyFullID(), tempShip.hull.energySlots.numberOf());
        tempShip.hull.weaponSlots.removeAll();
        int i = 0;
        while (i < tempShip.hull.weaponSlots.numberOf()) {
            if (rng().nextInt(2) == 1) {
                tempShip.hull.equipWeapon(gear.weaponBay(), 1);
            } else {
                tempShip.hull.equipWeapon(gear.weaponLaser(), 1);
            }
            i++;
        }
        int level = gear.levelMin() + rng().nextInt(gear.levelMax() - gear.levelMin() + 1);
        switch (rng().nextInt(9)) {
            case 0: tempShip.classSkill.set(level, 2); break;
            case 1: tempShip.classSkill.set(level, 1); break;
            case 2: tempShip.classSkill.set(level, 4); break;
            case 3: tempShip.classSkill.set(level, 3); break;
            default: tempShip.classSkill.set(level, 0);
        }
        long creditDrop = gear.creditMin() + rng().nextInt((int) (gear.creditMax() - gear.creditMin() + 1));
        tempShip.cargo.setCurrency(creditDrop);
        tempShip.setCustomTag(NameDatabase.getRandomMachineShipName());
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