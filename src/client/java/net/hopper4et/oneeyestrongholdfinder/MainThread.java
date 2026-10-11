package net.hopper4et.oneeyestrongholdfinder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Samples a locally thrown eye on the client thread, then prints candidate strongholds. */
public final class MainThread {
    private static final int SAMPLE_TICKS = 80;

    private final Entity eyeOfEnder;
    private final Vec3 firstPosition;
    private int ticks;

    public MainThread(Entity eyeOfEnder) {
        this.eyeOfEnder = eyeOfEnder;
        this.firstPosition = eyeOfEnder.position();
    }

    /** @return true once the sample was processed. */
    public boolean tick() {
        if (++ticks < SAMPLE_TICKS) return false;
        find(eyeOfEnder.position());
        return true;
    }

    private void find(Vec3 lastPosition) {
        Vec3 pos1 = firstPosition;
        Vec3 pos2 = lastPosition;
        if (pos1.x == pos2.x || pos1.z == pos2.z) return;

        boolean isReverse = false;
        if (Math.abs(pos1.z - pos2.z) > Math.abs(pos1.x - pos2.x)) {
            pos1 = new Vec3(pos1.z, pos1.y, pos1.x);
            pos2 = new Vec3(pos2.z, pos2.y, pos2.x);
            isReverse = true;
        }

        GridFinder finder = new GridFinder(
                pos1.x, pos1.z,
                (pos2.z - pos1.z) / (pos2.x - pos1.x),
                (pos2.x - pos1.x) < 0 ? -1 : 1
        );

        for (int i = 0; !finder.isInRing(); i++) {
            finder.next();
            if (i > 500) return;
        }

        List<Stronghold> strongholds = new ArrayList<>();
        while (finder.isInRing()) {
            Stronghold stronghold = finder.next();
            if (stronghold.accuracy > 2) strongholds.add(stronghold);
        }
        if (strongholds.isEmpty()) return;
        strongholds.sort(Comparator.comparingLong((Stronghold candidate) -> candidate.accuracy).reversed());

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        addChatMessage(Component.literal("=== One Eye Stronghold Finder ===")
                .withStyle(style -> style.withColor(0x9BFBFF)));

        int shown = Math.min(strongholds.size(), 5);
        long minAccuracy = strongholds.get(Math.min(4, strongholds.size() - 1)).accuracy;
        long maxAccuracy = strongholds.getFirst().accuracy;
        for (int i = 0; i < shown; i++) {
            Stronghold stronghold = strongholds.get(i);
            int overworldX = (isReverse ? stronghold.z : stronghold.x) + 3;
            int overworldZ = (isReverse ? stronghold.x : stronghold.z) + 3;
            int netherX = overworldX / 8;
            int netherZ = overworldZ / 8;
            int color = maxAccuracy == minAccuracy ? 255
                    : (int) ((stronghold.accuracy - minAccuracy) * 255.0 / (maxAccuracy - minAccuracy));
            MutableComponent message = Component.literal(String.format(
                    "X: %- 6d Z: %- 6d (Nether: X: %- 6d Z: %- 6d) accuracy: %d",
                    overworldX, overworldZ, netherX, netherZ, stronghold.accuracy));
            addChatMessage(message.withStyle(style -> style.withColor(
                    new Color(255 - color, color, 2).getRGB())
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Copy to clipboard")))
                    .withClickEvent(new ClickEvent.CopyToClipboard(overworldX + " ~ " + overworldZ))));
        }
    }

    private static void addChatMessage(Component message) {
        //? if >=26.2 {
        /*Minecraft.getInstance().gui.chatListener().handleSystemMessage(message, false);
        *///?} else if >=26.1 {
        /*Minecraft.getInstance().gui.getChat().addClientSystemMessage(message);
        *///?} else {
        Minecraft.getInstance().gui.getChat().addMessage(message);
        //?}
    }
}
