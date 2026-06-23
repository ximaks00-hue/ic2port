package dev.ic2port.villager;

import com.google.common.collect.ImmutableSet;
import dev.ic2port.Reference;
import dev.ic2port.setup.BlockRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/**
 * IC2 Classic villager professions unlocked by placing workstation blocks.
 */
public final class Ic2VillagerProfessions {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, Reference.MOD_ID);

    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, Reference.MOD_ID);

    public static final RegistryObject<PoiType> ELECTRIC_POI = POI_TYPES.register("electric",
            () -> new PoiType(states(BlockRegistry.ELECTRIC_FURNACE.get()), 1, 1));

    public static final RegistryObject<PoiType> NUCLEAR_POI = POI_TYPES.register("nuclear",
            () -> new PoiType(states(BlockRegistry.NUCLEAR_REACTOR.get()), 1, 1));

    public static final RegistryObject<PoiType> CROP_POI = POI_TYPES.register("crop",
            () -> new PoiType(states(BlockRegistry.CROP_ANALYZER.get()), 1, 1));

    public static final RegistryObject<PoiType> DEMO_POI = POI_TYPES.register("demo",
            () -> new PoiType(states(BlockRegistry.SOLID_FUEL_GENERATOR.get()), 1, 1));

    public static final RegistryObject<PoiType> BREWING_POI = POI_TYPES.register("brewing",
            () -> new PoiType(states(BlockRegistry.BREWING_BARREL.get()), 1, 1));

    public static final RegistryObject<PoiType> GREG_POI = POI_TYPES.register("greg",
            () -> new PoiType(states(BlockRegistry.CONSTRUCTION_FOAM.get()), 1, 1));

    public static final RegistryObject<VillagerProfession> ELECTRIC = PROFESSIONS.register("electric",
            () -> profession(ELECTRIC_POI, SoundEvents.VILLAGER_WORK_MASON));

    public static final RegistryObject<VillagerProfession> NUCLEAR = PROFESSIONS.register("nuclear",
            () -> profession(NUCLEAR_POI, SoundEvents.VILLAGER_WORK_LIBRARIAN));

    public static final RegistryObject<VillagerProfession> CROP = PROFESSIONS.register("crop",
            () -> profession(CROP_POI, SoundEvents.VILLAGER_WORK_FARMER));

    public static final RegistryObject<VillagerProfession> DEMO = PROFESSIONS.register("demo",
            () -> profession(DEMO_POI, SoundEvents.VILLAGER_WORK_WEAPONSMITH));

    public static final RegistryObject<VillagerProfession> BREWING = PROFESSIONS.register("brewing",
            () -> profession(BREWING_POI, SoundEvents.VILLAGER_WORK_CLERIC));

    public static final RegistryObject<VillagerProfession> GREG = PROFESSIONS.register("greg",
            () -> profession(GREG_POI, SoundEvents.VILLAGER_WORK_LIBRARIAN));

    private Ic2VillagerProfessions() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        POI_TYPES.register(modEventBus);
        PROFESSIONS.register(modEventBus);
    }

    private static VillagerProfession profession(
            final RegistryObject<PoiType> poi,
            final net.minecraft.sounds.SoundEvent workSound) {
        return new VillagerProfession(
                poi.getKey().location().toString(),
                holder -> holder.is(poi.getKey()),
                holder -> holder.is(poi.getKey()),
                ImmutableSet.of(),
                ImmutableSet.of(),
                workSound);
    }

    private static Set<BlockState> states(final Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }
}
