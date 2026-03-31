package com.example.addon.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileApi extends Module {

    public FileApi() {
        super(Categories.Misc, "FileApi", "Handles tochat.txt, command.txt, print.txt, lastchat.txt");
    }

    @Override
    public void onActivate() {
        File folder = new File("FileApi");
        if (!folder.exists()) folder.mkdir();
    }



    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // tochat
        File tochat = new File("FileApi/tochat.txt");
        if (tochat.exists()) {
            try {
                String message = new String(Files.readAllBytes(tochat.toPath()), StandardCharsets.UTF_8).trim();
                if (!message.isEmpty()) {
                    mc.getNetworkHandler().sendChatMessage(message);
                }
                Files.write(tochat.toPath(), new byte[0]); // clear file
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // print
        File print = new File("FileApi/print.txt");
        if (print.exists()) {
            try {
                String message = new String(Files.readAllBytes(print.toPath()), StandardCharsets.UTF_8).trim();
                if (!message.isEmpty()) {
                    mc.inGameHud.getChatHud().addMessage(Text.literal(message));
                }
                Files.write(print.toPath(), new byte[0]); // clear file
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        // command
        File file = new File("FileApi/command.txt");
        if (!file.exists()) return;
        try {
            String message = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
            if (!message.isEmpty()) {
                mc.getNetworkHandler().sendChatCommand(message);
                Files.write(file.toPath(), new byte[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @EventHandler
    private void onPacket(PacketEvent.Receive event) {

        // lastchat
        if (event.packet instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket) event.packet;
            String message = packet.content().getString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("FileApi/lastchat.txt", false))) {
                writer.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
