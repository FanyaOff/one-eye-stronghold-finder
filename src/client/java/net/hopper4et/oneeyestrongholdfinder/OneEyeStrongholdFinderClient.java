package net.hopper4et.oneeyestrongholdfinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OneEyeStrongholdFinderClient implements ClientModInitializer {
    private static final int THROW_MATCH_TICKS = 20;
    private static final double THROW_MATCH_DISTANCE_SQUARED = 4.0;
    private static final List<ThrowAttempt> THROW_ATTEMPTS = new ArrayList<>();
    private static final Map<Integer, MainThread> TRACKED_EYES = new HashMap<>();
    private static Object trackedLevel;

    @Override
    public void onInitializeClient() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() && player instanceof LocalPlayer
                    && player.getItemInHand(hand).is(Items.ENDER_EYE)) {
                ensureLevel(world);
                THROW_ATTEMPTS.add(new ThrowAttempt(player.getEyePosition(), world.getGameTime()));
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof EyeOfEnder)) return;
            ensureLevel(world);

            ThrowAttempt matched = null;
            double nearest = THROW_MATCH_DISTANCE_SQUARED;
            for (ThrowAttempt attempt : THROW_ATTEMPTS) {
                long elapsed = world.getGameTime() - attempt.gameTime;
                double distance = entity.position().distanceToSqr(attempt.playerPosition);
                if (elapsed >= 0 && elapsed <= THROW_MATCH_TICKS && distance < nearest) {
                    nearest = distance;
                    matched = attempt;
                }
            }
            if (matched != null) {
                THROW_ATTEMPTS.remove(matched);
                TRACKED_EYES.put(entity.getId(), new MainThread(entity));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                trackedLevel = null;
                THROW_ATTEMPTS.clear();
                TRACKED_EYES.clear();
                return;
            }
            ensureLevel(client.level);
            long now = client.level.getGameTime();
            THROW_ATTEMPTS.removeIf(attempt -> now - attempt.gameTime > THROW_MATCH_TICKS);

            Iterator<Map.Entry<Integer, MainThread>> iterator = TRACKED_EYES.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, MainThread> tracked = iterator.next();
                if (tracked.getValue().tick()) iterator.remove();
            }
        });
    }

    private static void ensureLevel(Object level) {
        if (trackedLevel != level) {
            trackedLevel = level;
            THROW_ATTEMPTS.clear();
            TRACKED_EYES.clear();
        }
    }

    private record ThrowAttempt(Vec3 playerPosition, long gameTime) {}
}
