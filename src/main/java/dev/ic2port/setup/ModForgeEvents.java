package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.item.IElectricItem;
import dev.ic2port.item.ElectricItemCapabilityProvider;
import dev.ic2port.item.NanoSaberItem;
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
        } else if (stack.getItem() instanceof NanoSaberItem saber) {
            event.addCapability(
                    ELECTRIC_ITEM_CAPABILITY_ID,
                    new ElectricItemCapabilityProvider(stack, asElectricItem(saber)));
        }
    }

    private static IElectricItem asElectricItem(final NanoSaberItem saber) {
        return new IElectricItem() {
            @Override
            public double getMaxEnergy() {
                return saber.getMaxEnergy();
            }

            @Override
            public int getTier() {
                return saber.getEnergyTier();
            }

            @Override
            public double getStoredEnergy(final ItemStack stack) {
                return saber.getStoredEnergy(stack);
            }

            @Override
            public void setStoredEnergy(final ItemStack stack, final double energy) {
                saber.setStoredEnergy(stack, energy);
            }

            @Override
            public double charge(final ItemStack stack, final double amount) {
                return saber.charge(stack, amount);
            }

            @Override
            public double drawEnergy(final ItemStack stack, final double amount) {
                return saber.drawEnergy(stack, amount);
            }
        };
    }
}
