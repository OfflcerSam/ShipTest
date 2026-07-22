package offlcersam.shiptest;

import illuminatus.core.datastructures.List;
import mods.ModLogger;
import game.weapons.WeaponTurretPlacement;

public class WeaponLayoutList {
    // Add layout variables up here.
    public static int ARROWHEAD_LAYOUT;
    public static int FOUNDRY_LAYOUT;
    public static int FOUNDRY_PLUS_LAYOUT;


    // Trying to mimic vanilla WeaponSlotLayoutList for fun
    public static List<WeaponTurretPlacement> layouts;
    private static WeaponTurretPlacement placement;
    public static WeaponTurretPlacement ZERO;

    static {ZERO = new WeaponTurretPlacement();}

    public static void init() {
        layouts = new List();
        ModLogger.log("[ShipTest] Loading custom weapon layouts...");

        placement = new WeaponTurretPlacement(); // Make variable new.
        placement.addSlot(-66.0, 21.0); // Add a slot with angle and distance from center of sprite.
        placement.addSlot(66.0, 21.0);
        placement.addSlot(-55.0, 16.0);
        placement.addSlot(55.0, 16.0);
        ARROWHEAD_LAYOUT = layouts.add(placement); // Add placements to layout.

        placement = new WeaponTurretPlacement(); // Make variable new.
        placement.addSlot(-45.0, 45.0); // Add a slot with angle and distance from center of sprite.
        placement.addSlot(45.0, 45.0);
        placement.addSlot(-30.0, 35.0);
        placement.addSlot(30.0, 35.0);
        placement.addSlot(-18.0, 50.0);
        placement.addSlot(18.0, 50.0);
        placement.addSlot(0.0, 50.0);
        FOUNDRY_LAYOUT = layouts.add(placement); // Add placements to layout.

        placement = new WeaponTurretPlacement(); // Make variable new.
        placement.addSlot(-45.0, 45.0); // Add a slot with angle and distance from center of sprite.
        placement.addSlot(45.0, 45.0);
        placement.addSlot(-30.0, 35.0);
        placement.addSlot(30.0, 35.0);
        placement.addSlot(-18.0, 50.0);
        placement.addSlot(18.0, 50.0);
        FOUNDRY_PLUS_LAYOUT = layouts.add(placement); // Add placements to layout.


        ModLogger.log("[ShipTest] Loaded: " + layouts.size() + " custom layouts.");
    }

    public static WeaponTurretPlacement get(int index) {
        if (index < 0) {
            return ZERO;
        }
        return layouts.get(index);
    }

    public static int getWeaponSlotCount(int index) {
        return WeaponLayoutList.get(index).getSlotCount();
    }
}
