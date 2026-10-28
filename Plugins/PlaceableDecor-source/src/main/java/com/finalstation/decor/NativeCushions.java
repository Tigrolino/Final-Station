package com.finalstation.decor;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Cushion;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * The only version-dependent code. Resolve every native method at startup, before accepting items.
 */
final class NativeCushions {
    private final Method playerHandle, worldHandle, entityHandle, asNative, useOn, consumesAction;
    private final Method getData, unsafeTag, stringOr, damageSources, playerAttack, hurt;
    private final Constructor<?> vector, blockPos, hitResult, context;
    private final Class<?> direction, hand;
    private final Object cushionItem, customData;

    NativeCushions() throws ReflectiveOperationException {
        Class<?> nativePlayer = type("world.entity.player.Player");
        Class<?> nativeEntity = type("world.entity.Entity");
        Class<?> level = type("world.level.Level");
        Class<?> stack = type("world.item.ItemStack");
        Class<?> vec = type("world.phys.Vec3");
        Class<?> pos = type("core.BlockPos");
        Class<?> hit = type("world.phys.BlockHitResult");
        Class<?> useContext = type("world.item.context.UseOnContext");
        direction = type("core.Direction");
        hand = type("world.InteractionHand");
        playerHandle =
                Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer").getMethod("getHandle");
        worldHandle = Class.forName("org.bukkit.craftbukkit.CraftWorld").getMethod("getHandle");
        entityHandle =
                Class.forName("org.bukkit.craftbukkit.entity.CraftEntity").getMethod("getHandle");
        asNative =
                Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack")
                        .getMethod("asNMSCopy", ItemStack.class);
        vector = vec.getConstructor(double.class, double.class, double.class);
        blockPos = pos.getConstructor(int.class, int.class, int.class);
        hitResult = hit.getConstructor(vec, direction, pos, boolean.class);
        context = useContext.getConstructor(level, nativePlayer, hand, stack, hit);
        Object temporary = asNative.invoke(null, new ItemStack(Material.WHITE_CUSHION));
        cushionItem = stack.getMethod("getItem").invoke(temporary);
        useOn = type("world.item.CushionItem").getMethod("useOn", useContext);
        consumesAction = type("world.InteractionResult").getMethod("consumesAction");
        customData = type("core.component.DataComponents").getField("CUSTOM_DATA").get(null);
        getData = stack.getMethod("get", type("core.component.DataComponentType"));
        unsafeTag = type("world.item.component.CustomData").getMethod("getUnsafe");
        stringOr = type("nbt.CompoundTag").getMethod("getStringOr", String.class, String.class);
        damageSources = nativeEntity.getMethod("damageSources");
        playerAttack =
                type("world.damagesource.DamageSources").getMethod("playerAttack", nativePlayer);
        hurt =
                type("world.entity.decoration.Cushion")
                        .getMethod(
                                "hurtServer",
                                type("server.level.ServerLevel"),
                                type("world.damagesource.DamageSource"),
                                float.class);
    }

    private static Class<?> type(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft." + name);
    }

    private static Object enumConstant(Class<?> type, String name) {
        for (Object value : type.getEnumConstants()) {
            if (((Enum<?>) value).name().equals(name)) return value;
        }
        throw new IllegalArgumentException("Missing native enum constant " + name);
    }

    boolean place(Player player, EquipmentSlot slot, Block block, BlockFace face, Vector point)
            throws ReflectiveOperationException {
        Object hit =
                hitResult.newInstance(
                        vector.newInstance(point.getX(), point.getY(), point.getZ()),
                        enumConstant(direction, face.name()),
                        blockPos.newInstance(block.getX(), block.getY(), block.getZ()),
                        false);
        // An isolated cushion stack lets vanilla calculate and place the entity without changing
        // the player's real flint stack, even on special collision shapes such as cauldrons.
        Object temporary = asNative.invoke(null, new ItemStack(Material.WHITE_CUSHION));
        Object useContext =
                context.newInstance(
                        worldHandle.invoke(block.getWorld()),
                        playerHandle.invoke(player),
                        enumConstant(hand, slot == EquipmentSlot.HAND ? "MAIN_HAND" : "OFF_HAND"),
                        temporary,
                        hit);
        return (boolean) consumesAction.invoke(useOn.invoke(cushionItem, useContext));
    }

    String customString(ItemStack item, String key) throws ReflectiveOperationException {
        Object data = getData.invoke(asNative.invoke(null, item), customData);
        return data == null ? "" : (String) stringOr.invoke(unsafeTag.invoke(data), key, "");
    }

    void punch(Cushion cushion, Player player) throws ReflectiveOperationException {
        Object handle = entityHandle.invoke(cushion);
        Object source =
                playerAttack.invoke(damageSources.invoke(handle), playerHandle.invoke(player));
        hurt.invoke(handle, worldHandle.invoke(cushion.getWorld()), source, 1.0f);
    }
}
