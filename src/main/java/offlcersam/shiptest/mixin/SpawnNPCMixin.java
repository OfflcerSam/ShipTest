package offlcersam.shiptest.mixin;

import _database.SpawnMacro;
import _database.SpawnNPC;
import game.objects.SpaceShip;
import game.world.Sector;
import illuminatus.core.datastructures.List;
import offlcersam.shiptest.NPCRegistrar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SpawnNPC.class, remap = false)
public class SpawnNPCMixin {

    @Inject(
            method = "spawnTieredMob(IIILgame/world/Sector;ZIIZII)Lilluminatus/core/datastructures/List;",
            at = @At("HEAD")
    )
    private static void shiptest$captureTier(int x, int y, int spawnPosSpread, Sector sector,
                                             boolean orphan, int mobSize, int hostilityConstant,
                                             boolean stayAtSpawn, int forceFaction, int tier,
                                             CallbackInfoReturnable<List<SpaceShip>> cir) {
        NPCRegistrar.stashTier(tier);
    }

    @Redirect(
            method = "spawnTieredMob(IIILgame/world/Sector;ZIIZII)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "L_database/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shiptest$redirectTieredMobShip(int xPos, int yPos, Sector sector,
                                                            int hostilityConstant, int spawnIndex,
                                                            int factionIndex) {
        int rolled = NPCRegistrar.rollTieredMob(NPCRegistrar.consumeStashedTier(), spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnPolice(Lgame/world/Sector;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "L_database/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shiptest$redirectPoliceShip(int xPos, int yPos, Sector sector,
                                                         int hostilityConstant, int spawnIndex,
                                                         int factionIndex) {
        int rolled = NPCRegistrar.rollPolice(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnTempPoliceMob(IIILgame/world/Sector;)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "L_database/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shiptest$redirectTempPoliceShip(int xPos, int yPos, Sector sector,
                                                             int hostilityConstant, int spawnIndex,
                                                             int factionIndex) {
        int rolled = NPCRegistrar.rollTempPolice(spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnRogueDrones(Lgame/world/Sector;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "L_database/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shiptest$redirectRogueDroneShip(int xPos, int yPos, Sector sector,
                                                             int hostilityConstant, int spawnIndex, int factionIndex,
                                                             Sector origSector, int origX, int origY, int tier) {
        int bucket = NPCRegistrar.rogueDroneBucket(tier);
        int rolled = NPCRegistrar.rollRogueDrone(bucket, spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnRogueDrones(Lgame/world/Sector;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "L_database/SpawnNPC;configRogueDrone(ILgame/objects/SpaceShip;)V"
            )
    )
    private static void shiptest$redirectRogueDroneConfig(int shipId, SpaceShip tempShip) {
        if (NPCRegistrar.isCustomRogueDrone(shipId)) {
            NPCRegistrar.configureCustomRogueDrone(shipId, tempShip);
        } else {
            SpawnNPCAccessor.invokeConfigRogueDrone(shipId, tempShip);
        }
    }

    @Redirect(
            method = "spawnTempRogueDrones(Lgame/world/Sector;IIIIIZ)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "L_database/SpawnMacro;generateShip(IILgame/world/Sector;III)Lgame/objects/SpaceShip;"
            )
    )
    private static SpaceShip shiptest$redirectTempRogueDroneShip(int xPos, int yPos, Sector sector,
                                                                 int hostilityConstant, int spawnIndex, int factionIndex,
                                                                 Sector origSector, int spawnPosSpread, int numberOf,
                                                                 int origX, int origY, int origHostility, boolean spawnLowTiers) {
        int sectorTier = spawnLowTiers ? sector.getSectorTier() / 2 : sector.getSectorTier();
        int bucket = NPCRegistrar.rogueDroneBucket(sectorTier);
        int rolled = NPCRegistrar.rollRogueDrone(bucket, spawnIndex);
        return SpawnMacro.generateShip(xPos, yPos, sector, hostilityConstant, rolled, factionIndex);
    }

    @Redirect(
            method = "spawnTempRogueDrones(Lgame/world/Sector;IIIIIZ)Lilluminatus/core/datastructures/List;",
            at = @At(
                    value = "INVOKE",
                    target = "L_database/SpawnNPC;configRogueDrone(ILgame/objects/SpaceShip;)V"
            )
    )
    private static void shiptest$redirectTempRogueDroneConfig(int shipId, SpaceShip tempShip) {
        if (NPCRegistrar.isCustomRogueDrone(shipId)) {
            NPCRegistrar.configureCustomRogueDrone(shipId, tempShip);
        } else {
            SpawnNPCAccessor.invokeConfigRogueDrone(shipId, tempShip);
        }
    }

    @Redirect(
            method = "spawnBoss(Lgame/world/Sector;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "L_database/SpawnNPC;spawnBoss(IILgame/world/Sector;I)V"
            )
    )
    private static void shiptest$redirectBoss(int shipIndex, int faction, Sector sector, int bossSlot) {
        int rolled = NPCRegistrar.rollBoss(sector.getSectorTier(), shipIndex);
        SpawnNPC.spawnBoss(rolled, faction, sector, bossSlot);
    }
}