package com.example.randomloot;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagedChestState extends PersistentState {
    private static final String DATA_KEY = RandomLootChestsMod.MOD_ID + "_managed_chests";

    private final Map<String, ManagedChestEntry> entries = new HashMap<>();
    private long tickCounter = 0;

    public static ManagedChestState get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        return overworld.getPersistentStateManager().getOrCreate(type(), DATA_KEY);
    }

    private static PersistentStateType<ManagedChestState> type() {
        return new PersistentStateType<>(ManagedChestState::new, ManagedChestState::fromNbt, null);
    }

    public boolean tickAndReady(long intervalTicks) {
        tickCounter++;
        if (tickCounter >= intervalTicks) {
            tickCounter = 0;
            markDirty();
            return true;
        }
        return false;
    }

    public void put(RegistryKey<net.minecraft.world.World> worldKey, BlockPos pos, RegistryKey<net.minecraft.loot.LootTable> lootTable) {
        entries.put(key(worldKey, pos), new ManagedChestEntry(worldKey, pos.toImmutable(), lootTable));
        markDirty();
    }

    public void remove(RegistryKey<net.minecraft.world.World> worldKey, BlockPos pos) {
        if (entries.remove(key(worldKey, pos)) != null) {
            markDirty();
        }
    }

    public List<ManagedChestEntry> copyEntries() {
        return new ArrayList<>(entries.values());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLong("tickCounter", tickCounter);
        NbtList list = new NbtList();
        for (ManagedChestEntry entry : entries.values()) {
            NbtCompound e = new NbtCompound();
            e.putString("world", entry.worldKey().getValue().toString());
            e.putLong("pos", entry.pos().asLong());
            e.putString("lootTable", entry.lootTable().getValue().toString());
            list.add(e);
        }
        nbt.put("entries", list);
        return nbt;
    }

    private static ManagedChestState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        ManagedChestState state = new ManagedChestState();
        state.tickCounter = nbt.getLong("tickCounter");
        NbtList list = nbt.getList("entries", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : list) {
            NbtCompound e = (NbtCompound) element;
            RegistryKey<net.minecraft.world.World> worldKey = RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(e.getString("world")));
            BlockPos pos = BlockPos.fromLong(e.getLong("pos"));
            RegistryKey<net.minecraft.loot.LootTable> lootTable = RegistryKey.of(net.minecraft.registry.RegistryKeys.LOOT_TABLE, Identifier.of(e.getString("lootTable")));
            state.entries.put(state.key(worldKey, pos), new ManagedChestEntry(worldKey, pos, lootTable));
        }
        return state;
    }

    private String key(RegistryKey<net.minecraft.world.World> worldKey, BlockPos pos) {
        return worldKey.getValue() + "|" + pos.asLong();
    }

    public record ManagedChestEntry(
            RegistryKey<net.minecraft.world.World> worldKey,
            BlockPos pos,
            RegistryKey<net.minecraft.loot.LootTable> lootTable
    ) {
    }
}
