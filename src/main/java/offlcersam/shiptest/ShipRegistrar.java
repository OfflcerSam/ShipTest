package offlcersam.shiptest;

import illuminatus.core.graphics.Color;
import items.ItemTypeConstantsInterface;
import items.TypeTag;
import items.lists.ShipList;
import mods.ModLogger;

import java.util.ArrayList;
import java.util.List;

import static offlcersam.shiptest.WeaponLayoutList.*;

public final class ShipRegistrar {
    private static boolean registered;

    // Stores the base IDs of every weapon we add
    private static final List<Integer> REGISTERED_SHIP_IDS = new ArrayList<>();

    private ShipRegistrar() { }

    // Registers a ship ID and remembers it for later use.
    private static int registerShipID(int id) {
        REGISTERED_SHIP_IDS.add(id);
        ModLogger.log("[ShipTest] Added ship ID to registry: " + id);
        return id;
    }

    // Returns database ID for all ships.
    public static int[] getShipDatabaseIDs() {
        int[] ids = new int[REGISTERED_SHIP_IDS.size()];

        for (int i = 0; i < REGISTERED_SHIP_IDS.size(); i++) {
            ids[i] = ItemTypeConstantsInterface.SHIP * 10000 + REGISTERED_SHIP_IDS.get(i);
        }
        return ids;
    }

    // Custom registration helper.
    private static void writeShip(int id, int icon, Color color, String name, String description, int tier, TypeTag rarity, int renderIndex, int engineDisplacement, float hull, float cargo, int weaponLayout, int energySlots, int armorSlots, int shieldSlots, int deviceSlots, int moduleSlots, int engineSlots)
    {
        ShipList.write(
                registerShipID(id),
                icon,
                color,
                name,
                description,
                tier,
                rarity,
                renderIndex,
                engineDisplacement,
                hull,
                cargo,
                weaponLayout,
                energySlots,
                armorSlots,
                shieldSlots,
                deviceSlots,
                moduleSlots,
                engineSlots
        );
    }




    public static void registerShips() {
        if (registered) { return; }
        registered = true;

        // Uses default cargoMod from ShipList.
        float cargoMod = 0.75F;
        float integ = 200.0F;
        float carg = 75.0F * cargoMod;

        writeShip(
                350,                         //Int: ID, unique ship ID
                30,                             // Int: Icon, sets Icon according to sprite sheet.
                Color.AZURE,                    // Color: Color, unsure what exactly this affects.
                "Arrowhead",                    // String: Display name
                "Maybe one day you could be a real arrow.", // String: Display description
                0,                              // Int: Tier, affects spawning and what level it's usable at.
                TypeTag.UNCOMMON,               // TypeTag, Affects spawning and loot drop, I think.
                350,                            // Int: Render Index, the ship's sprite, currently there is a index limit somewhere near 350-400 until it is upped.
                37,                             // Int: Engine Position glow in pixels
                integ * 1.50F,                  // Float: Hull HP (integ * multiplier), somewhat based off ShipList style of doing it.
                carg * 1.10F,                   // Float: Cargo (carg * multiplier), also based off ShipList style of doing it.
                ARROWHEAD_LAYOUT,               // WeaponSlotLayoutList: Weapon Layout, see WeaponSlotLayoutList for full list or make your own.
                2,                              // Int: Energy slots, unsure what the UI limit for slots are but base game doesn't go above 8 currently.
                1,                              // Int: Armor slots
                1,                              // Int: Shield slots
                0,                              // Int: Device slots
                1,                              // Int: Module slots
                1                               // Int: Engine slots
        );

        integ = 225.0F;
        carg = 350.0F * cargoMod * 2.0F;
        writeShip(40, 158, Color.WHITE, "Foundry", "Build an even bigger megastructure.", 4, TypeTag.RARE, 349, 64, integ * 1.20F, carg * 1.3F, FOUNDRY_LAYOUT, 6, 5, 4, 2, 5, 4);
        writeShip(41, 216, Color.PURPLE, "Foundry+", "Build an even bigger megastructure+.", 5, TypeTag.EXOTIC, 349, 64, integ * 1.5F, carg * 1.5F, FOUNDRY_PLUS_LAYOUT, 6, 6, 5, 3, 6, 5);


        ShipList.loadShipStatsFromItems(_database.ItemDatabase.itemDataFile);
        ModLogger.log("[ShipTest] Registered " + REGISTERED_SHIP_IDS.size() + " ships");
    }
}
