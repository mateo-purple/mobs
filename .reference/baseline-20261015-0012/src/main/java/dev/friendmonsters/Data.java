package dev.friendmonsters;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/** Persistent data is stored by NeoForge with each original vanilla entity. */
public final class Data {
    private Data() {}
    private static String key(String name) { return "fm_" + name; }
    public static CompoundTag tag(Entity e) { return e.getPersistentData(); }
    public static String text(Entity e, String name) { return tag(e).getString(key(name)).orElse(""); }
    public static int number(Entity e, String name) { return tag(e).getInt(key(name)).orElse(0); }
    public static long time(Entity e, String name) { return tag(e).getLong(key(name)).orElse(0L); }
    public static double decimal(Entity e, String name) { return tag(e).getDouble(key(name)).orElse(0.0); }
    public static boolean flag(Entity e, String name) { return tag(e).getBoolean(key(name)).orElse(false); }
    public static void set(Entity e, String k, String v) { tag(e).putString(key(k), v); }
    public static void set(Entity e, String k, int v) { tag(e).putInt(key(k), v); }
    public static void set(Entity e, String k, long v) { tag(e).putLong(key(k), v); }
    public static void set(Entity e, String k, double v) { tag(e).putDouble(key(k), v); }
    public static void set(Entity e, String k, boolean v) { tag(e).putBoolean(key(k), v); }
    public static void remove(Entity e, String k) { tag(e).remove(key(k)); }
    public static String kind(Entity e) { return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath(); }
    public static boolean owned(Entity e) { return !text(e, "owner").isEmpty(); }
    public static UUID ownerId(Entity e) {
        try { return UUID.fromString(text(e, "owner")); } catch (IllegalArgumentException ex) { return null; }
    }
    public static ServerPlayer owner(Entity e) {
        if (!(e.level() instanceof ServerLevel level)) return null;
        UUID id = ownerId(e);
        return id == null ? null : level.getServer().getPlayerList().getPlayer(id);
    }
    public static Rules.Mode mode(Entity e) {
        int n = number(e, "mode");
        return n >= 0 && n < Rules.Mode.values().length ? Rules.Mode.values()[n] : Rules.Mode.FOLLOW;
    }
    public static void mode(Entity e, Rules.Mode mode) { set(e, "mode", mode.ordinal()); }
    public static boolean controlled(Entity e) {
        return owned(e) && !Rules.NON_COMBAT.contains(kind(e)) && mode(e) != Rules.Mode.DISMISSED;
    }
    public static <T> void save(Entity e, String name, Codec<T> codec, T value) {
        var ops = e.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        codec.encodeStart(ops, value).resultOrPartial(FriendMonsters.LOGGER::error)
            .ifPresent(t -> tag(e).put(key(name), t));
    }
    public static <T> T load(Entity e, String name, Codec<T> codec, T fallback) {
        Tag value = tag(e).get(key(name));
        if (value == null) return fallback;
        return codec.parse(e.registryAccess().createSerializationContext(NbtOps.INSTANCE), value)
            .resultOrPartial(FriendMonsters.LOGGER::error).orElse(fallback);
    }
    public static List<ItemStack> items(Entity e, String name) {
        return new ArrayList<>(load(e, name, ItemStack.CODEC.listOf(), List.of()));
    }
    public static void items(Entity e, String name, List<ItemStack> items) {
        save(e, name, ItemStack.CODEC.listOf(), items.stream().filter(s -> !s.isEmpty()).toList());
    }
    public static void anchor(Entity e) {
        set(e,"anchor_x",e.getX()); set(e,"anchor_y",e.getY()); set(e,"anchor_z",e.getZ());
    }
}
