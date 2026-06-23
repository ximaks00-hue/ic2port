package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.item.IElectricItem;
import dev.ic2port.menu.ElectricEnchanterMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.util.ContainerDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Electric enchant/disenchant station for EU-powered tools only.
 */
public class ElectricEnchanterBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 100_000.0D;
    public static final double ENERGY_PER_TICK = 500.0D;
    public static final int TIER = EnergyTier.HV;
    public static final int MAX_STORED_XP = 1000;

    public static final int TAB_ENCHANT = 0;
    public static final int TAB_DISENCHANT = 1;

    public static final int SLOT_TOOL = 0;
    public static final int SLOT_LAPIS = 1;
    public static final int SLOT_BOOK = 2;
    public static final int SLOT_COUNT = 3;

    private int activeTab;
    private int storedXp;
    private int selectedEnchantmentIndex;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.round(getStoredEnergy());
                case 1 -> (int) Math.round(getCapacity());
                case 2 -> activeTab;
                case 3 -> storedXp;
                case 4 -> selectedEnchantmentIndex;
                case 5 -> getEnchantmentOptions().size();
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public ElectricEnchanterBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.ELECTRIC_ENCHANTER_BE.get(), pos, state, SLOT_COUNT, ENERGY_CAPACITY);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final ElectricEnchanterBlockEntity enchanter) {
        enchanter.tickServer();
    }

    private void tickServer() {
        if (!isServerProcessingEnabled() || getStoredEnergy() < ENERGY_PER_TICK) {
            return;
        }
        if (activeTab == TAB_DISENCHANT) {
            tryDisenchant();
        }
    }

    public void setActiveTab(final int tab) {
        activeTab = tab == TAB_DISENCHANT ? TAB_DISENCHANT : TAB_ENCHANT;
        setChanged();
    }

    public void setSelectedEnchantmentIndex(final int index) {
        selectedEnchantmentIndex = Math.max(0, index);
        setChanged();
    }

    public void tryEnchant(final Player player) {
        if (level == null || level.isClientSide || activeTab != TAB_ENCHANT) {
            return;
        }
        ItemStack tool = getItemHandler().getStackInSlot(SLOT_TOOL);
        ItemStack lapis = getItemHandler().getStackInSlot(SLOT_LAPIS);
        if (!isElectricTool(tool) || lapis.isEmpty() || lapis.getCount() < 3) {
            return;
        }
        var options = getEnchantmentOptions();
        if (selectedEnchantmentIndex < 0 || selectedEnchantmentIndex >= options.size()) {
            return;
        }
        Map.Entry<Enchantment, Integer> entry = options.get(selectedEnchantmentIndex);
        int xpCost = entry.getValue() * 2;
        if (storedXp < xpCost || getStoredEnergy() < ENERGY_PER_TICK * 20) {
            return;
        }
        Map<Enchantment, Integer> current = EnchantmentHelper.getEnchantments(tool);
        current.put(entry.getKey(), entry.getValue());
        EnchantmentHelper.setEnchantments(current, tool);
        lapis.shrink(3);
        getItemHandler().setStackInSlot(SLOT_LAPIS, lapis.isEmpty() ? ItemStack.EMPTY : lapis);
        getItemHandler().setStackInSlot(SLOT_TOOL, tool);
        storedXp -= xpCost;
        consumeEnergy(ENERGY_PER_TICK * 20);
        setChanged();
    }

    private void tryDisenchant() {
        ItemStack tool = getItemHandler().getStackInSlot(SLOT_TOOL);
        ItemStack book = getItemHandler().getStackInSlot(SLOT_BOOK);
        if (!isElectricTool(tool) || book.getCount() >= book.getMaxStackSize()) {
            return;
        }
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(tool));
        if (enchantments.isEmpty() || storedXp < 5 || getStoredEnergy() < ENERGY_PER_TICK) {
            return;
        }
        Map.Entry<Enchantment, Integer> top = enchantments.entrySet().iterator().next();
        enchantments.remove(top.getKey());
        EnchantmentHelper.setEnchantments(enchantments, tool);
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(enchantedBook,
                new net.minecraft.world.item.enchantment.EnchantmentInstance(top.getKey(), top.getValue()));
        if (book.isEmpty()) {
            getItemHandler().setStackInSlot(SLOT_BOOK, enchantedBook);
        } else if (ItemStack.isSameItemSameTags(book, enchantedBook)) {
            book.grow(1);
            getItemHandler().setStackInSlot(SLOT_BOOK, book);
        } else {
            return;
        }
        getItemHandler().setStackInSlot(SLOT_TOOL, tool);
        storedXp -= 5;
        consumeEnergy(ENERGY_PER_TICK);
        setChanged();
    }

    public void storePlayerXp(final Player player, final int amount) {
        if (amount <= 0) {
            return;
        }
        int transferable = Math.min(amount, MAX_STORED_XP - storedXp);
        if (transferable <= 0) {
            return;
        }
        if (player.experienceLevel > 0 || player.experienceProgress > 0.0F) {
            player.giveExperiencePoints(-transferable);
            storedXp += transferable;
            setChanged();
        }
    }

    private boolean isElectricTool(final ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IElectricItem;
    }

    public java.util.List<Map.Entry<Enchantment, Integer>> getEnchantmentOptions() {
        ItemStack tool = getItemHandler().getStackInSlot(SLOT_TOOL);
        if (!isElectricTool(tool) || level == null) {
            return java.util.List.of();
        }
        return EnchantmentHelper.getAvailableEnchantmentResults(30, tool, false)
                .stream()
                .map(option -> Map.entry(option.enchantment, option.level))
                .toList();
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return false;
    }

    @Override
    protected boolean isProcessSlotInput(final int processSlot) {
        return processSlot == SLOT_TOOL || processSlot == SLOT_LAPIS || processSlot == SLOT_BOOK;
    }

    @Override
    protected boolean canAutomationExtractFromSlot(final int processSlot) {
        return processSlot == SLOT_BOOK;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.electric_enchanter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new ElectricEnchanterMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ActiveTab", activeTab);
        tag.putInt("StoredXp", storedXp);
        tag.putInt("SelectedEnchantment", selectedEnchantmentIndex);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        activeTab = tag.getInt("ActiveTab");
        storedXp = tag.getInt("StoredXp");
        selectedEnchantmentIndex = tag.getInt("SelectedEnchantment");
    }
}
