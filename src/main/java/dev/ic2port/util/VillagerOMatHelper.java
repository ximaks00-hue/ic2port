package dev.ic2port.util;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Villager scanning and automated trade execution for the Villager-O-Mat.
 */
public final class VillagerOMatHelper {

    public static final int MAX_TRACKED_VILLAGERS = 16;
    public static final int MAX_TRADES = 12;

    private VillagerOMatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<UUID> scanVillagers(final Level level, final BlockPos origin, final int radius) {
        List<UUID> villagers = new ArrayList<>();
        for (Villager villager : level.getEntitiesOfClass(
                Villager.class,
                new net.minecraft.world.phys.AABB(origin).inflate(radius))) {
            if (!villager.isAlive() || villager.isBaby()) {
                continue;
            }
            villagers.add(villager.getUUID());
            if (villagers.size() >= MAX_TRACKED_VILLAGERS) {
                break;
            }
        }
        return villagers;
    }

    @Nullable
    public static Villager findVillager(final Level level, final BlockPos origin, final UUID uuid, final int radius) {
        if (level == null) {
            return null;
        }
        for (Villager villager : level.getEntitiesOfClass(
                Villager.class,
                new net.minecraft.world.phys.AABB(origin).inflate(radius))) {
            if (villager.getUUID().equals(uuid)) {
                return villager;
            }
        }
        return null;
    }

    public static boolean canAfford(final ItemStackHandler inputs, final int inputSlots, final MerchantOffer offer) {
        return countMatching(inputs, inputSlots, offer.getCostA()) >= offer.getCostA().getCount()
                && countMatching(inputs, inputSlots, offer.getCostB()) >= offer.getCostB().getCount();
    }

    public static boolean executeTrade(
            final ItemStackHandler inventory,
            final int inputSlots,
            final int outputStart,
            final int outputSlots,
            final MerchantOffer offer) {
        if (offer.isOutOfStock() || offer.getResult().isEmpty()) {
            return false;
        }
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        ItemStack result = offer.getResult().copy();

        if (!canAfford(inventory, inputSlots, offer)) {
            return false;
        }
        if (!canFit(inventory, outputStart, outputSlots, result)) {
            return false;
        }
        if (!consume(inventory, inputSlots, costA) || !consume(inventory, inputSlots, costB)) {
            return false;
        }
        OutputBufferHelper.insertRange(inventory, outputStart, outputSlots, result);
        offer.increaseUses();
        return true;
    }

    public static int tradeCount(final @Nullable Villager villager) {
        if (villager == null) {
            return 0;
        }
        MerchantOffers offers = villager.getOffers();
        return offers == null ? 0 : offers.size();
    }

    private static int countMatching(final ItemStackHandler handler, final int inputSlots, final ItemStack required) {
        if (required.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        int total = 0;
        for (int slot = 0; slot < inputSlots; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (ItemStack.isSameItemSameTags(stack, required)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static boolean consume(final ItemStackHandler handler, final int inputSlots, final ItemStack required) {
        if (required.isEmpty()) {
            return true;
        }
        int needed = required.getCount();
        for (int slot = 0; slot < inputSlots && needed > 0; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!ItemStack.isSameItemSameTags(stack, required)) {
                continue;
            }
            int take = Math.min(needed, stack.getCount());
            stack.shrink(take);
            handler.setStackInSlot(slot, stack);
            needed -= take;
        }
        return needed <= 0;
    }

    private static boolean canFit(
            final ItemStackHandler handler,
            final int outputStart,
            final int outputSlots,
            final ItemStack stack) {
        ItemStack remainder = OutputBufferHelper.insertRange(handler, outputStart, outputSlots, stack);
        return remainder.isEmpty();
    }
}
