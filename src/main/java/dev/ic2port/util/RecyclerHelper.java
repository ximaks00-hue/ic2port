package dev.ic2port.util;

import dev.ic2port.Reference;
import dev.ic2port.api.reactor.IReactorComponent;
import dev.ic2port.api.reactor.IReactorFuel;
import dev.ic2port.item.IElectricItem;
import dev.ic2port.item.IUpgradeItem;
import dev.ic2port.item.RadioactiveItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

/**
 * Determines whether an item can be processed in the recycler.
 */
public final class RecyclerHelper {

    private static final Set<String> RECYCLABLE_MOD_BLOCKS = Set.of(
            "tin_ore",
            "deepslate_tin_ore",
            "uranium_ore",
            "deepslate_uranium_ore",
            "basic_machine_casing",
            "advanced_machine_casing",
            "rubber_wood",
            "rubber_sapling",
            "rubber_leaves",
            "contaminated_soil");

    private RecyclerHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean canRecycle(final ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.isEnchanted() || stack.getDamageValue() > 0) {
            return false;
        }
        if (stack.is(ItemRegistry.SCRAP.get())
                || stack.is(ItemRegistry.SCRAP_BOX.get())
                || stack.is(ItemRegistry.UU_MATTER.get())) {
            return false;
        }
        if (stack.getItem() instanceof IElectricItem || stack.getItem() instanceof IUpgradeItem) {
            return false;
        }
        if (stack.getItem() instanceof IReactorFuel || stack.getItem() instanceof IReactorComponent
                || stack.getItem() instanceof RadioactiveItem) {
            return false;
        }
        if (stack.is(ItemRegistry.NUCLEAR_REACTOR.get()) || stack.is(ItemRegistry.REACTOR_CHAMBER.get())) {
            return false;
        }
        if (stack.is(ItemRegistry.PLUTONIUM.get())
                || stack.is(ItemRegistry.DEPLETED_FUEL_ROD.get())
                || stack.is(ItemRegistry.DEPLETED_URANIUM.get())
                || stack.is(ItemRegistry.FUEL_ROD.get())
                || stack.is(ItemRegistry.MOX_FUEL_ROD.get())
                || stack.is(ItemRegistry.URANIUM_INGOT.get())
                || stack.is(ItemRegistry.IRIDIUM.get())
                || stack.is(ItemRegistry.WRENCH.get())
                || stack.is(ItemRegistry.OD_SCANNER.get())
                || stack.is(ItemRegistry.DIAMOND_DRILL.get())
                || stack.is(ItemRegistry.ADVANCED_DRILL.get())
                || stack.is(ItemRegistry.CENTRIFUGE_ROTOR.get())
                || stack.is(ItemRegistry.RECYCLER.get())) {
            return false;
        }
        if (stack.is(Items.BEDROCK) || stack.is(Items.BARRIER) || stack.is(Items.COMMAND_BLOCK)) {
            return false;
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            final ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(blockItem.getBlock());
            if (blockId != null && Reference.MOD_ID.equals(blockId.getNamespace())) {
                return RECYCLABLE_MOD_BLOCKS.contains(blockId.getPath());
            }
        }
        return true;
    }
}
