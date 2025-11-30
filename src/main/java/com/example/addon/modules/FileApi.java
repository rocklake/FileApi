//package meteordevelopment.meteorclient.systems.modules.misc;
package com.example.addon.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileApi extends Module {

    public FileApi() {
        super(Categories.Misc, "FileApi", "A File API tochat.txt command.txt lastchat.txt");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        File folder = new File("FileApi");
        if (!folder.exists()) folder.mkdir();

        if (mc.player == null || mc.world == null) return;

        try {
            File file = new File("FileApi/tochat.txt");
            if (file.exists()) {
                String message = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim();
                if (!message.isEmpty()) {

                    mc.getNetworkHandler().sendChatMessage(message);

                    java.nio.file.Files.write(file.toPath(), new byte[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File("FileApi/print.txt");
        if (!file.exists()) return;

        try {
            String message = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim();
            if (!message.isEmpty()) {
                mc.inGameHud.getChatHud().addMessage(Text.literal(message));
                java.nio.file.Files.write(file.toPath(), new byte[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
}    

    }
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || !mc.player.isAlive()) return;

        File folder = new File("FileApi");
        if (!folder.exists()) folder.mkdir();

        File file = new File("FileApi/command.txt");
        if (!file.exists()) return;

        try {
            String message = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim();
        if (!message.isEmpty()) {
            mc.getNetworkHandler().sendChatCommand(message); // send command
            java.nio.file.Files.write(file.toPath(), new byte[0]); // clear file
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    private void onChatPacket(PacketEvent.Receive event) {
        if (event.packet instanceof GameMessageS2CPacket packet) {

            String message = packet.content().getString();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("FileApi/lastchat.txt", false))) {
                writer.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
