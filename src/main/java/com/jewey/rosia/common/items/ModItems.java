package com.jewey.rosia.common.items;

import com.jewey.rosia.Rosia;
import net.dries007.tfc.util.Metal;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.dries007.tfc.common.items.MoldItem;

import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Rosia.MOD_ID);


    public static final RegistryObject<Item> COPPER_WIRE = ITEMS.register("copper_wire",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));

    public static final RegistryObject<Item> COPPER_COIL = ITEMS.register("copper_coil",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));

    public static final RegistryObject<Item> INVAR_INGOT = ITEMS.register("invar_ingot",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));

    public static final RegistryObject<Item> INVAR_DOUBLE_INGOT = ITEMS.register("invar_double_ingot",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));

    public static final RegistryObject<Item> INVAR_SHEET = ITEMS.register("invar_sheet",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));

    public static final RegistryObject<Item> MAGNETITE_POWDER = ITEMS.register("magnetite_powder",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));

    public static final RegistryObject<Item> COMPRESSED_MAGNETITE = ITEMS.register("compressed_magnetite",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));

    public static final RegistryObject<Item> MAGNET = ITEMS.register("magnet",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));

    public static final RegistryObject<Item> STEEL_GRINDSTONE = ITEMS.register("steel_grindstone",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB).durability(750)));

    public static final RegistryObject<Item> MOTOR = ITEMS.register("motor",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));

    public static final RegistryObject<Item> WRENCH_HEAD = ITEMS.register("wrench_head",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.ROSIA_TAB)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }


}
