package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.CropRegisterEvent;
import dev.ic2port.api.recipes.MachineRecipeRegistryEvent;
import dev.ic2port.crop.CropRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Fires addon API events during mod lifecycle.
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AddonApiForgeEvents {

  private AddonApiForgeEvents() {
    throw new UnsupportedOperationException("Utility class");
  }

  @SubscribeEvent
  public static void onCommonSetup(final FMLCommonSetupEvent event) {
    event.enqueueWork(() -> MinecraftForge.EVENT_BUS.post(new MachineRecipeRegistryEvent(MachineRecipeRegistry.INSTANCE)));
  }

  /**
   * Invoked from {@link CropRegistry#bootstrap()} after built-in crops are registered.
   */
  public static void fireCropRegisterEvent() {
    MinecraftForge.EVENT_BUS.post(new CropRegisterEvent(CropRegistry::register));
  }
}
