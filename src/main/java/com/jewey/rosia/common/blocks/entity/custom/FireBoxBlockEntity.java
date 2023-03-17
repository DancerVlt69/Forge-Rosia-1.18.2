package com.jewey.rosia.common.blocks.entity.custom;

import com.jewey.rosia.common.blocks.custom.fire_box;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.container.FireBoxContainer;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.BellowsBlockEntity;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Fuel;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IntArrayBuilder;
import net.dries007.tfc.util.calendar.ICalendarTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;

import static com.jewey.rosia.Rosia.MOD_ID;

public class FireBoxBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements ICalendarTickable, MenuProvider
{


    public Component getDisplayName() {
        return new TextComponent("Fire-Box");
    }

    public static final int SLOT_FUEL_MIN = 0;
    public static final int SLOT_FUEL_MAX = 2;

    private static final TranslatableComponent NAME = Helpers.translatable(MOD_ID + ".block_entity.fire_box");


    public static void serverTick(Level level, BlockPos pos, BlockState state, FireBoxBlockEntity forge)
    {
        forge.checkForLastTickSync();
        forge.checkForCalendarUpdate();

        if (forge.needsRecipeUpdate)
        {
            forge.needsRecipeUpdate = false;
        }

        boolean isRaining = level.isRainingAt(pos);
        if (state.getValue(fire_box.HEAT) > 0)
        {
            if (isRaining && level.random.nextFloat() < 0.15F)
            {
                Helpers.playSound(level, pos, SoundEvents.LAVA_EXTINGUISH);
            }
            int heatLevel = Mth.clamp((int) (forge.temperature / Heat.maxVisibleTemperature() * 6) + 1, 1, 7); // scaled 1 through 7
            if (heatLevel != state.getValue(fire_box.HEAT))
            {
                level.setBlockAndUpdate(pos, state.setValue(fire_box.HEAT, heatLevel));
                forge.markForSync();
            }

            // Update fuel
            if (forge.burnTicks > 0)
            {
                forge.burnTicks -= forge.airTicks > 0 || isRaining ? 2 : 1; // Fuel burns twice as fast using bellows, or in the rain
            }
            if (forge.burnTicks <= 0 && !forge.consumeFuel())
            {
                forge.extinguish(state);
            }
        }
        else if (forge.burnTemperature > 0)
        {
            forge.extinguish(state);
        }
        if (forge.airTicks > 0)
        {
            forge.airTicks--;
        }

        // Always update temperature / cooking, until the fire pit is not hot anymore
        if (forge.temperature > 0 || forge.burnTemperature > 0)
        {
            forge.temperature = HeatCapability.adjustDeviceTemp(forge.temperature, forge.burnTemperature, forge.airTicks, isRaining);

            HeatCapability.provideHeatTo(level, pos.above(), forge.temperature);

            forge.markForSync();
        }

        // This is here to avoid duplication glitches
        if (forge.needsSlotUpdate)
        {
            forge.cascadeFuelSlots();
        }
    }

    protected final ContainerData syncableData;
    private final HeatingRecipe[] cachedRecipes = new HeatingRecipe[5];
    private boolean needsSlotUpdate = false;
    private float temperature; // Current Temperature
    private int burnTicks; // Ticks remaining on the current item of fuel
    private float burnTemperature; // Temperature provided from the current item of fuel
    private int airTicks; // Ticks of air provided by bellows
    private long lastPlayerTick; // Last player tick this forge was ticked (for purposes of catching up)
    private boolean needsRecipeUpdate; // Set to indicate on tick, the cached recipes need to be re-updated

    public FireBoxBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.FIRE_BOX_BLOCK_ENTITY.get(), pos, state, defaultInventory(3), NAME);

        temperature = 0;
        burnTemperature = 0;
        burnTicks = 0;
        airTicks = 0;
        lastPlayerTick = Integer.MIN_VALUE;
        syncableData = new IntArrayBuilder().add(() -> (int) temperature, value -> temperature = value);

        if (TFCConfig.SERVER.charcoalForgeEnableAutomation.get())
        {
            sidedInventory
                    .on(new PartialItemHandler(inventory).insert(SLOT_FUEL_MIN, 1, SLOT_FUEL_MAX), Direction.Plane.HORIZONTAL);
        }

        Arrays.fill(cachedRecipes, null);
    }

    public void intakeAir(int amount)
    {
        airTicks += amount;
        if (airTicks > BellowsBlockEntity.MAX_DEVICE_AIR_TICKS)
        {
            airTicks = BellowsBlockEntity.MAX_DEVICE_AIR_TICKS;
        }
    }

    @Override
    public void onCalendarUpdate(long ticks)
    {
        assert level != null;
        final BlockState state = level.getBlockState(worldPosition);
        if (state.getValue(fire_box.HEAT) != 0)
        {
            HeatCapability.Remainder remainder =
                    HeatCapability.consumeFuelForTicks(ticks, inventory, burnTicks, burnTemperature, SLOT_FUEL_MIN, SLOT_FUEL_MAX);

            burnTicks = remainder.burnTicks();
            burnTemperature = remainder.burnTemperature();
            needsSlotUpdate = true;

            if (remainder.ticks() > 0)
            {
                // Consumed all fuel, so extinguish and cool instantly
                extinguish(state);
            }
        }
    }

    @Override
    @Deprecated
    public long getLastUpdateTick()
    {
        return lastPlayerTick;
    }

    @Override
    @Deprecated
    public void setLastUpdateTick(long tick)
    {
        lastPlayerTick = tick;
    }

    public ContainerData getSyncableData()
    {
        return syncableData;
    }

    public float getTemperature()
    {
        return temperature;
    }

    public int getAirTicks()
    {
        return airTicks;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInv, Player player)
    {
        return FireBoxContainer.create(this, playerInv, windowID);
    }

    @Override
    public void loadAdditional(CompoundTag nbt)
    {
        temperature = nbt.getFloat("temperature");
        burnTicks = nbt.getInt("burnTicks");
        airTicks = nbt.getInt("airTicks");
        burnTemperature = nbt.getFloat("burnTemperature");
        lastPlayerTick = nbt.getLong("lastPlayerTick");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        nbt.putFloat("temperature", temperature);
        nbt.putInt("burnTicks", burnTicks);
        nbt.putInt("airTicks", airTicks);
        nbt.putFloat("burnTemperature", burnTemperature);
        nbt.putLong("lastPlayerTick", lastPlayerTick);
        super.saveAdditional(nbt);
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);
        needsSlotUpdate = true;
    }

    @Override
    public int getSlotStackLimit(int slot)
    {
        return 4;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        if (slot <= SLOT_FUEL_MAX)
        {
            return Helpers.isItem(stack.getItem(), TFCTags.Items.FORGE_FUEL);
        }
        else
        {
            return stack.getCapability(Capabilities.FLUID_ITEM).isPresent() && stack.getCapability(HeatCapability.CAPABILITY).isPresent();
        }
    }

    /**
     * Attempts to light the forge. Use over just setting the block state HEAT, as if there is no fuel, that will light the forge for one tick which looks strange
     *
     * @param state The current firepit block state
     * @return {@code true} if the firepit was lit.
     */
    public boolean light(BlockState state)
    {
        assert level != null;
        if (consumeFuel())
        {
            level.setBlockAndUpdate(worldPosition, state.setValue(fire_box.HEAT, 2));
            return true;
        }
        return false;
    }

    /**
     * Attempts to consume one piece of fuel. Returns if the fire pit consumed any fuel (and so, ended up lit)
     */
    private boolean consumeFuel()
    {
        final ItemStack fuelStack = inventory.getStackInSlot(SLOT_FUEL_MIN);
        if (!fuelStack.isEmpty())
        {
            // Try and consume a piece of fuel
            inventory.extractItem(SLOT_FUEL_MIN, 1, false);
            needsSlotUpdate = true;
            Fuel fuel = Fuel.get(fuelStack);
            if (fuel != null)
            {
                burnTicks += fuel.getDuration() * 1.1;      // 10% more efficient
                burnTemperature = fuel.getTemperature();
            }
            markForSync();
        }
        return burnTicks > 0;
    }

    private void extinguish(BlockState state)
    {
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.setValue(fire_box.HEAT, 0));
        burnTicks = 0;
        burnTemperature = 0;
        markForSync();
    }

    private void cascadeFuelSlots() {
        // move from slot 1 to 0 if match and not empty
        if(inventory.getStackInSlot(0) == inventory.getStackInSlot(1)
                && inventory.getStackInSlot(1) != ItemStack.EMPTY) {
            inventory.extractItem(1,1, false);
            inventory.extractItem(0,-1, false);
        }
        // move stack from slot 1 to 0 if empty
        else if(inventory.getStackInSlot(0) == ItemStack.EMPTY
                && inventory.getStackInSlot(1) != ItemStack.EMPTY ) {
            inventory.setStackInSlot(0, inventory.getStackInSlot(1).copy());
            inventory.setStackInSlot(1, ItemStack.EMPTY);
        }
        // move from slot 2 to 0 if match and not empty
        else if(inventory.getStackInSlot(0) == inventory.getStackInSlot(2)
                && inventory.getStackInSlot(2) != ItemStack.EMPTY
                && inventory.getStackInSlot(1) == ItemStack.EMPTY) {
            inventory.extractItem(2,1, false);
            inventory.extractItem(0,-1, false);
        }
        // move stack from slot 2 to 0 if empty
        else if(inventory.getStackInSlot(0) == ItemStack.EMPTY
                && inventory.getStackInSlot(2) != ItemStack.EMPTY ) {
            inventory.setStackInSlot(0, inventory.getStackInSlot(2).copy());
            inventory.setStackInSlot(2, ItemStack.EMPTY);
        }
    }

    /**
     * Add button to light the firebox
     */



}