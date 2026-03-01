package com.example.randomloot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomLootChestsMod implements ModInitializer {
    public static final String MOD_ID = "randomlootchests";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final long CHECK_INTERVAL_TICKS = 2L * 60L * 60L * 20L;

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) ->
                ManagedChestState.get(world.getServer()).remove(world.getRegistryKey(), pos));

        LOGGER.info("Random Loot Chests initialized");
    }

    private void onServerTick(MinecraftServer server) {
        ManagedChestState state = ManagedChestState.get(server);
        if (!state.tickAndReady(CHECK_INTERVAL_TICKS)) {
            return;
        }

        for (ManagedChestState.ManagedChestEntry entry : state.copyEntries()) {
            ServerWorld world = server.getWorld(entry.worldKey());
            if (world == null) {
                continue;
            }

            BlockEntity blockEntity = world.getBlockEntity(entry.pos());
            if (!(blockEntity instanceof ChestBlockEntity chest)) {
                state.remove(entry.worldKey(), entry.pos());
                continue;
            }

            if (isInventoryEmpty(chest)) {
                refillChestFromLootTable(world, chest, entry.lootTable());
                chest.markDirty();
            }
        }
    }

    private boolean isInventoryEmpty(ChestBlockEntity chest) {
        for (int i = 0; i < chest.size(); i++) {
            if (!chest.getStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void refillChestFromLootTable(ServerWorld world, ChestBlockEntity chest, RegistryKey<LootTable> lootTableKey) {
        chest.setLootTable(lootTableKey, world.random.nextLong());
        chest.generateLoot(null);
    }

    public static RegistryKey<LootTable> lootTableForName(Text name) {
        String chestName = name.getString();
        if ("Loot".equals(chestName)) {
            return RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(MOD_ID, "chests/loot"));
        }
        if ("Loot +1".equals(chestName)) {
            return RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(MOD_ID, "chests/loot_plus_1"));
        }
        if ("Loot +2".equals(chestName)) {
            return RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(MOD_ID, "chests/loot_plus_2"));
        }
        return null;
    }

    public static void maybeRegisterManagedChest(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (!player.hasPermissionLevel(2)) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chest) || !chest.hasCustomName()) {
            return;
        }

        RegistryKey<LootTable> tableKey = lootTableForName(chest.getCustomName());
        if (tableKey == null) {
            return;
        }

        ManagedChestState.get(world.getServer()).put(world.getRegistryKey(), pos, tableKey);
    }
}
