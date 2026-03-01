package com.example.randomloot.mixin;

import com.example.randomloot.RandomLootChestsMod;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("RETURN"))
    private void randomlootchests$afterPlace(ItemPlacementContext context, net.minecraft.block.BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        if (!(context.getPlayer() instanceof ServerPlayerEntity player)) {
            return;
        }

        if (!(context.getWorld() instanceof ServerWorld world)) {
            return;
        }

        RandomLootChestsMod.maybeRegisterManagedChest(player, world, context.getBlockPos());
    }
}
