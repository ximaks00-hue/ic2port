package dev.ic2port.setup;

import dev.ic2port.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Mod fluids — steam is a dedicated fluid type (not vanilla water).
 */
public final class ModFluids {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, Reference.MOD_ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Reference.MOD_ID);

    public static final RegistryObject<FluidType> STEAM_TYPE = FLUID_TYPES.register("steam",
            () -> new FluidType(FluidType.Properties.create()
                    .density(-1000)
                    .viscosity(500)
                    .temperature(500)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
                @Override
                public void initializeClient(final Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private static final ResourceLocation STEAM_STILL =
                                new ResourceLocation(Reference.MOD_ID, "block/steam_still");
                        private static final ResourceLocation STEAM_FLOW =
                                new ResourceLocation(Reference.MOD_ID, "block/steam_flow");

                        @Override
                        public @NotNull ResourceLocation getStillTexture() {
                            return STEAM_STILL;
                        }

                        @Override
                        public @NotNull ResourceLocation getFlowingTexture() {
                            return STEAM_FLOW;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFFFFFFFF;
                        }
                    });
                }
            });

    public static final RegistryObject<ForgeFlowingFluid> STEAM =
            FLUIDS.register("steam", () -> new ForgeFlowingFluid.Source(steamProperties()));
    public static final RegistryObject<ForgeFlowingFluid> STEAM_FLOWING =
            FLUIDS.register("flowing_steam", () -> new ForgeFlowingFluid.Flowing(steamProperties()));

    private static ForgeFlowingFluid.Properties steamProperties() {
        return new ForgeFlowingFluid.Properties(STEAM_TYPE, STEAM, STEAM_FLOWING);
    }

    private ModFluids() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final net.minecraftforge.eventbus.api.IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
