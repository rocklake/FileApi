package com.example.addon.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
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
        super(Categories.Misc, "FileApi", "Handles tochat.txt, command.txt, print.txt, lastchat.txt, player_name.txt, player_move_c2s_packet.txt with directional movement.");
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
                if (!message.isEmpty()) mc.getNetworkHandler().sendChatMessage(message);
                Files.write(tochat.toPath(), new byte[0]); // clear file
            } catch (IOException e) { e.printStackTrace(); }
        }

        // print
        File print = new File("FileApi/print.txt");
        if (print.exists()) {
            try {
                String message = new String(Files.readAllBytes(print.toPath()), StandardCharsets.UTF_8).trim();
                if (!message.isEmpty()) mc.inGameHud.getChatHud().addMessage(Text.literal(message));
                Files.write(print.toPath(), new byte[0]); // clear file
            } catch (IOException e) { e.printStackTrace(); }
        }

        // player_move_c2s_packet
        File moveFile = new File("FileApi/player_move_c2s_packet.txt");
        if (moveFile.exists()) {
            try {
                String s = new String(Files.readAllBytes(moveFile.toPath()), StandardCharsets.UTF_8).trim();
                if (!s.isEmpty()) {
                    JsonElement root = JsonParser.parseString(s);
                    java.util.function.Consumer<JsonObject> send = obj -> {
                        try {
                            mc.inGameHud.getChatHud().addMessage(Text.literal("FileApi: reading move packet"));
                            double x = obj.has("x") ? parseRelative(obj.get("x").getAsString(), mc.player.getX()) : mc.player.getX();
                            double y = obj.has("y") ? parseRelative(obj.get("y").getAsString(), mc.player.getY()) : mc.player.getY();
                            double z = obj.has("z") ? parseRelative(obj.get("z").getAsString(), mc.player.getZ()) : mc.player.getZ();
                            boolean onGround = obj.has("onGround") && obj.get("onGround").getAsBoolean();
                            mc.player.setPosition(x, y, z);
                            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionAndOnGround(
                                x, y, z, onGround, false
                            );
                            mc.getNetworkHandler().sendPacket(packet);

                            mc.inGameHud.getChatHud().addMessage(Text.literal("FileApi: packet sent"));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    };
                    if (root.isJsonArray()) root.getAsJsonArray().forEach(e -> { if (e.isJsonObject()) send.accept(e.getAsJsonObject()); });
                    else if (root.isJsonObject()) send.accept(root.getAsJsonObject());
                }
                Files.write(moveFile.toPath(), new byte[0]); // clear file
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        // command
        File commandFile = new File("FileApi/command.txt");
        if (commandFile.exists()) {
            try {
                String message = new String(Files.readAllBytes(commandFile.toPath()), StandardCharsets.UTF_8).trim();
                if (!message.isEmpty()) {
                    mc.getNetworkHandler().sendChatCommand(message);
                    Files.write(commandFile.toPath(), new byte[0]);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }

        // player_name
        File playerNameFile = new File("FileApi/player_name.txt");
        try {
            String name = mc.player.getName().getString();
            Files.write(playerNameFile.toPath(), name.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        // lastchat
        if (event.packet instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = (GameMessageS2CPacket) event.packet;
            String message = packet.content().getString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("FileApi/lastchat.txt", false))) {
                writer.write(message);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private double parseRelative(String s, double current) {
        s = s.trim();
        if (s.startsWith("~")) {
            if (s.length() == 1) return current;
            return current + Double.parseDouble(s.substring(1));
        } else {
            return Double.parseDouble(s);
        }
    }
}
