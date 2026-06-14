package com.valaphee.edgejump;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class EdgeJump implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("edge-jump");
    public static boolean edgeJumpEnabled = true;
    public static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.edge-jump.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.edge-jump"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                edgeJumpEnabled = !edgeJumpEnabled;
                if (client.player != null) {
                    client.player.sendMessage(
                            net.minecraft.text.Text.translatable(edgeJumpEnabled
                                    ? "msg.edge-jump.enabled"
                                    : "msg.edge-jump.disabled"),
                            true
                    );
                }
                saveConfig();
            }
        });

        loadConfig();
    }

    private static void loadConfig() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("edgejump.json");
        if (!Files.exists(file)) return;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject config = new Gson().fromJson(reader, JsonObject.class);
            edgeJumpEnabled = config.get("edgeJump").getAsBoolean();
        } catch (Exception ex) {
            LOGGER.warn("Failed to load config", ex);
        }
    }

    private static void saveConfig() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("edgejump.json");
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            JsonObject config = new JsonObject();
            config.addProperty("edgeJump", edgeJumpEnabled);
            new Gson().toJson(config, writer);
        } catch (Exception ex) {
            LOGGER.warn("Failed to save config", ex);
        }
    }
}
