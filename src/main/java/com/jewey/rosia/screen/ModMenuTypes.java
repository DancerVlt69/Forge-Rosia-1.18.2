package com.jewey.rosia.screen;

import com.jewey.rosia.Rosia;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, Rosia.MOD_ID);

    public static final RegistryObject<MenuType<AutoQuernMenu>> AUTO_QUERN_MENU =
            registerMenuType(AutoQuernMenu::new, "auto_quern_menu");

    public static final RegistryObject<MenuType<NickelIronBatteryMenu>> NICKEL_IRON_BATTERY_MENU =
            registerMenuType(NickelIronBatteryMenu::new, "nickel_iron_battery_menu");



    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
