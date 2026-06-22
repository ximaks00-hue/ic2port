package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.item.IElectricItem;
import dev.ic2port.item.ElectricItemCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModForgeEvents {

    private static final ResourceLocation ELECTRIC_ITEM_CAPABILITY_ID =
            new ResourceLocation(Reference.MOD_ID, "electric_item");

    private ModForgeEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void attachItemCapabilities(final AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() instanceof IElectricItem electricItem) {
            event.addCapability(
                    ELECTRIC_ITEM_CAPABILITY_ID,
                    new ElectricItemCapabilityProvider(stack, electricItem));
        }
    }
}
