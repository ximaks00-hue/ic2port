package dev.ic2port.setup;



import dev.ic2port.Reference;

import dev.ic2port.item.EnergyShieldModuleItem;

import dev.ic2port.util.ArmorModuleHelper;

import net.minecraft.world.damagesource.DamageTypes;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.entity.living.LivingHurtEvent;

import net.minecraftforge.eventbus.api.EventPriority;

import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;



import java.util.List;

import java.util.Optional;



@Mod.EventBusSubscriber(modid = Reference.MOD_ID)

public final class EnergyShieldModuleForgeEvents {



    private EnergyShieldModuleForgeEvents() {

        throw new UnsupportedOperationException("Utility class");

    }



    @SubscribeEvent(priority = EventPriority.HIGH)

    public static void onLivingHurt(final LivingHurtEvent event) {

        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {

            return;

        }



        ItemStack chestplate = ArmorModuleHelper.getChestplate(player);

        Optional<Integer> index = ArmorModuleHelper.findModuleIndex(chestplate, EnergyShieldModuleItem.class);

        if (index.isEmpty()) {

            return;

        }



        List<ItemStack> modules = ArmorModuleHelper.getModules(chestplate);

        ItemStack module = modules.get(index.get());

        boolean fall = event.getSource().is(DamageTypes.FALL);

        float remaining = EnergyShieldModuleItem.absorbDamage(module, event.getAmount(), fall);

        if (remaining >= event.getAmount()) {

            return;

        }



        modules.set(index.get(), module);

        ArmorModuleHelper.setModules(chestplate, modules);

        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, chestplate);

        event.setAmount(remaining);

    }

}

