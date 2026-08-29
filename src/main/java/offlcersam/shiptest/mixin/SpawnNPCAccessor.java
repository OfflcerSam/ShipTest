package offlcersam.shiptest.mixin;

import _database.SpawnNPC;
import game.objects.SpaceShip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// As populateShipGear() and configRogueDrone() are both PRIVATE static methods on SpawnNPC, calling code (like NPCRegistrar) can't reach them directly.
// This interface mixin lets Mixin generate bridge methods into SpawnNPC at weave time so it can be called from outside the class.
// The method bodies below are never actually run as Mixin replaces them.
@Mixin(value = SpawnNPC.class, remap = false)
public interface SpawnNPCAccessor {

    @Invoker("populateShipGear")
    static void invokePopulateShipGear(SpaceShip spaceship, int tier, int pilotBehaviour, boolean equipTractor) {
        throw new AssertionError();
    }

    @Invoker("configRogueDrone")
    static void invokeConfigRogueDrone(int shipId, SpaceShip tempShip) {
        throw new AssertionError();
    }
}