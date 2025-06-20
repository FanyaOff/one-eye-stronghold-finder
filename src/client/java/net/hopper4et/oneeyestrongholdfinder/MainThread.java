package net.hopper4et.oneeyestrongholdfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainThread extends Thread {
    private final Entity eyeOfEnder;
    public MainThread(Entity eyeOfEnder) {
        this.eyeOfEnder = eyeOfEnder;
    }

    @Override
    public void run() {
        //взятие первых и вторых координат
        Vec3d pos1 = eyeOfEnder.getPos();
        try {Thread.sleep(4000);} catch (InterruptedException e) {return;}
        Vec3d pos2 = eyeOfEnder.getPos();
        if (pos1.x == pos2.x || pos1.z == pos2.z) return;

        //менять координаты для точности
        boolean isReverse = false;
        if (Math.abs(pos1.z - pos2.z) - Math.abs(pos1.x - pos2.x) > 0) {
            pos1 = new Vec3d(pos1.z, pos1.y, pos1.x);
            pos2 = new Vec3d(pos2.z, pos2.y, pos2.x);
            isReverse = true;
        }

        //создаёт искателя координат
        GridFinder finder = new GridFinder(
                pos1.x, pos1.z,
                (pos2.z - pos1.z) / (pos2.x - pos1.x),
                (pos2.x - pos1.x) < 0 ? -1 : 1
        );

        //идёт до кольца
        for (int i = 0; !finder.isInRing(); i++) {
            finder.next();
            if (i > 500) return;
        }

        //идёт по кольцу
        ArrayList<Stronghold> strongholds = new ArrayList<>();
        while (finder.isInRing()) {
            Stronghold stronghold = finder.next();
            if (stronghold.accuracy > 2) strongholds.add(stronghold);
        }
        if (strongholds.isEmpty()) return;

        //сортирует
        strongholds.sort(Comparator.comparing(a -> a.accuracy));
        Collections.reverse(strongholds);

        //создание переменных для цвета чата
        double minAccuracy = 0;
        double colorRatio = 0;
        if (strongholds.size() > 1) {
            minAccuracy = strongholds.get(Math.min(4, strongholds.size() - 1)).accuracy;
            colorRatio = (strongholds.get(0).accuracy - minAccuracy) / 510;
        }

        //вывод информации в чат
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        player.sendMessage(Text.literal("=== One Eye Stronghold Finder ===").setStyle(
                Style.EMPTY.withColor(new Color(155, 251, 255).getRGB())
        ), false);

        for (int i = 0; i < Math.min(strongholds.size(), 5); i++) {
            int color = (strongholds.size() > 1) ? (int) ((strongholds.get(i).accuracy - minAccuracy) / colorRatio) : 510;

            int overworldX = (isReverse ? strongholds.get(i).z : strongholds.get(i).x) + 3;
            int overworldZ = (isReverse ? strongholds.get(i).x : strongholds.get(i).z) + 3;
            int netherX = overworldX / 8;
            int netherZ = overworldZ / 8;

            String clickText = overworldX + " ~ " + overworldZ;

            player.sendMessage(Text.literal(String.format(
                    "X: %- 6d Z: %- 6d (Nether: X: %- 6d Z: %- 6d) accuracy: %d",
                    overworldX, overworldZ,
                    netherX, netherZ,
                    strongholds.get(i).accuracy
            )).setStyle(Style.EMPTY
                    .withColor(new Color(Math.min(255, 510 - color), Math.min(255, color), 2).getRGB())
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Copy to clipboard")))
                    .withClickEvent(new ClickEvent.CopyToClipboard(clickText))
            ), false);
        }
    }


}
