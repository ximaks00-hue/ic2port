package dev.ic2port.setup;



import dev.ic2port.Reference;

import dev.ic2port.item.ElectricJetpackItem;

import dev.ic2port.item.JetpackModuleItem;

import dev.ic2port.util.ArmorModuleHelper;

import dev.ic2port.util.JetpackHelper;

import dev.ic2port.util.PlayerInputHelper;

import net.minecraft.core.particles.ParticleTypes;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.entity.EquipmentSlot;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.TickEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;



import java.util.List;

import java.util.Optional;



@Mod.EventBusSubscriber(modid = Reference.MOD_ID)

public final class JetpackForgeEvents {



    private JetpackForgeEvents() {

        throw new UnsupportedOperationException("Utility class");

    }



    @SubscribeEvent

    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END || !event.side.isServer()) {

            return;

        }



        Player player = event.player;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        if (chest.getItem() instanceof ElectricJetpackItem jetpack) {

            if (JetpackHelper.applyThrust(player, chest, ElectricJetpackItem.CAPACITY, jetpack.getMode(chest))) {

                spawnSmoke(player);

            }

            return;

        }



        Optional<Integer> moduleIndex = JetpackHelper.findInstalledJetpackIndex(chest);

        if (moduleIndex.isEmpty()) {

            return;

        }



        List<ItemStack> modules = ArmorModuleHelper.getModules(chest);

        ItemStack module = modules.get(moduleIndex.get());

        if (!(module.getItem() instanceof JetpackModuleItem jetpackModule)) {

            return;

        }



        ElectricJetpackItem.JetpackMode mode = jetpackModule.getMode(module);

        if (JetpackHelper.applyThrust(player, module, JetpackModuleItem.CAPACITY, mode)) {

            modules.set(moduleIndex.get(), module);

            ArmorModuleHelper.setModules(chest, modules);

            player.setItemSlot(EquipmentSlot.CHEST, chest);

            spawnSmoke(player);

        }

    }



    private static void spawnSmoke(final Player player) {

        if (!(player.level() instanceof ServerLevel serverLevel)) {

            return;

        }

        serverLevel.sendParticles(

                ParticleTypes.SMOKE,

                player.getX(),

                player.getY() + 0.2D,

                player.getZ(),

                4,

                0.1D,

                0.05D,

                0.1D,

                0.01D);

    }

}

