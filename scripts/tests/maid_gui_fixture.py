"""Minimal loader/UI doubles matching the new TLM 26.x public GUI event contract."""

def sources(production):
    result = {
        'com/micaftic/morpher/client/compat/touhoulittlemaid/OfficialTouhouLittleMaidCompat.java': production,
        'net/neoforged/bus/api/Event.java': 'package net.neoforged.bus.api; public class Event {}',
        'net/neoforged/neoforge/common/NeoForge.java': '''package net.neoforged.neoforge.common;
public class NeoForge {
    public static Bus EVENT_BUS = new Bus();
    public static class Bus {
        public java.util.function.Consumer<net.neoforged.bus.api.Event> listener;
        public <T extends net.neoforged.bus.api.Event> void addListener(Class<T> type, java.util.function.Consumer<T> handler) {
            listener = event -> handler.accept(type.cast(event));
        }
    }
}''',
        'net/neoforged/fml/ModList.java': '''package net.neoforged.fml;
public class ModList {
    public static ModList get() { return new ModList(); }
    public boolean isLoaded(String id) {
        return "foxmodelloader".equals(id) || ("yes_steve_model".equals(id) && Boolean.getBoolean("fixture.ysm"));
    }
}''',
        'net/neoforged/api/distmarker/Dist.java': 'package net.neoforged.api.distmarker; public enum Dist { CLIENT, DEDICATED_SERVER }',
        'net/neoforged/fml/loading/FMLEnvironment.java': '''package net.neoforged.fml.loading;
public class FMLEnvironment { public static net.neoforged.api.distmarker.Dist getDist() { return net.neoforged.api.distmarker.Dist.CLIENT; } }''',
        'org/apache/logging/log4j/Logger.java': '''package org.apache.logging.log4j;
public class Logger { public void info(String text) {} public void debug(String text, Object value) {} }''',
        'com/micaftic/morpher/core/compat/touhoulittlemaid/TouhouLittleMaidAccess.java': '''package com.micaftic.morpher.core.compat.touhoulittlemaid;
public class TouhouLittleMaidAccess {
    public static boolean isLoaded() { return !Boolean.getBoolean("fixture.noMaid"); }
    public static boolean isMaid(net.minecraft.world.entity.Entity entity) { return true; }
}''',
        'net/minecraft/world/entity/Entity.java': '''package net.minecraft.world.entity;
public class Entity { public int getId() { return 42; } public java.util.UUID getUUID() { return new java.util.UUID(0,42); } }''',
        'net/minecraft/network/chat/Component.java': '''package net.minecraft.network.chat;
public class Component { public static Component literal(String text) { return new Component(); } public static Component translatable(String key) { return new Component(); } }''',
        'net/minecraft/client/gui/screens/Screen.java': 'package net.minecraft.client.gui.screens; public class Screen {}',
        'net/minecraft/client/gui/components/AbstractWidget.java': 'package net.minecraft.client.gui.components; public class AbstractWidget {}',
        'net/minecraft/client/gui/components/Tooltip.java': '''package net.minecraft.client.gui.components;
public class Tooltip { public static Tooltip create(net.minecraft.network.chat.Component text) { return new Tooltip(); } }''',
        'net/minecraft/client/gui/components/Button.java': '''package net.minecraft.client.gui.components;
public class Button extends AbstractWidget {
    public java.util.function.Consumer<Button> click;
    public static Button builder(net.minecraft.network.chat.Component text, java.util.function.Consumer<Button> click) {
        Button b = new Button(); b.click = click; return b;
    }
    public Button bounds(int x, int y, int w, int h) { return this; }
    public Button build() { return this; }
    public void setTooltip(Tooltip tooltip) {}
}''',
        'com/micaftic/morpher/client/gui/ModernPlayerModelScreen.java': '''package com.micaftic.morpher.client.gui;
public class ModernPlayerModelScreen extends net.minecraft.client.gui.screens.Screen {
    public java.util.function.BiConsumer<String,String> selection;
    public net.minecraft.client.gui.screens.Screen parent;
    public ModernPlayerModelScreen(net.minecraft.client.gui.screens.Screen parent, java.util.function.BiConsumer<String,String> selection, String key) {
        this.parent = parent; this.selection = selection;
    }
}''',
        'com/micaftic/morpher/util/InputUtil.java': '''package com.micaftic.morpher.util;
public class InputUtil {
    public static net.minecraft.client.gui.screens.Screen screen;
    public static void setScreen(net.minecraft.client.gui.screens.Screen value) { screen = value; }
    public static net.minecraft.client.gui.screens.Screen getCurrentScreen() { return screen; }
}''',
        'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
public class Minecraft {
    public net.minecraft.client.gui.screens.Screen screen;
    public static Minecraft getInstance() { return new Minecraft(); }
    public void setScreen(net.minecraft.client.gui.screens.Screen value) { com.micaftic.morpher.util.InputUtil.setScreen(value); }
}''',
        'com/micaftic/morpher/network/NetworkHandler.java': '''package com.micaftic.morpher.network;
public class NetworkHandler { public static Object sent; public static void sendToServer(Object packet) { sent = packet; } }''',
        'com/micaftic/morpher/network/message/C2SSetMaidModelPacket.java': '''package com.micaftic.morpher.network.message;
public record C2SSetMaidModelPacket(int entityId, String modelId, String textureId) {}''',
        'net/minecraft/network/protocol/common/custom/CustomPacketPayload.java': 'package net.minecraft.network.protocol.common.custom; public interface CustomPacketPayload {}',
        'net/neoforged/neoforge/client/network/ClientPacketDistributor.java': '''package net.neoforged.neoforge.client.network;
public class ClientPacketDistributor { public static void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {} }''',
        'com/github/tartaricacid/touhoulittlemaid/api/event/client/MaidContainerGuiEvent.java': '''package com.github.tartaricacid.touhoulittlemaid.api.event.client;
public class MaidContainerGuiEvent extends net.neoforged.bus.api.Event {
    public static class Init extends MaidContainerGuiEvent {
        public Object gui;
        public java.util.Map<String, net.minecraft.client.gui.components.AbstractWidget> buttons = new java.util.HashMap<>();
        public Init(Object gui) { this.gui = gui; }
        public Object getGui() { return gui; }
        public int getLeftPos() { return 10; }
        public int getTopPos() { return 20; }
        public void addButton(String name, net.minecraft.client.gui.components.AbstractWidget button) { buttons.putIfAbsent(name, button); }
    }
}''',
        'Fixture.java': '''
import net.neoforged.neoforge.common.NeoForge;
import com.github.tartaricacid.touhoulittlemaid.api.event.client.MaidContainerGuiEvent;
import com.micaftic.morpher.client.gui.ModernPlayerModelScreen;
import com.micaftic.morpher.network.NetworkHandler;
import com.micaftic.morpher.network.message.C2SSetMaidModelPacket;
import com.micaftic.morpher.util.InputUtil;
public class Fixture {
    public static class Menu { public net.minecraft.world.entity.Entity getMaid() { return new net.minecraft.world.entity.Entity(); } }
    public static class Gui extends net.minecraft.client.gui.screens.Screen { public Menu getMenu() { return new Menu(); } }
    static void init() throws Exception {
        Class<?> type = Class.forName("com.micaftic.morpher.client.compat.touhoulittlemaid.OfficialTouhouLittleMaidCompat");
        var method = type.getDeclaredMethod("init", org.apache.logging.log4j.Logger.class);
        method.setAccessible(true); method.invoke(null, new org.apache.logging.log4j.Logger());
    }
    public static void main(String[] args) throws Exception {
        init();
        if (NeoForge.EVENT_BUS.listener == null) throw new AssertionError("Current TLM GUI event was not registered");
        Gui parent = new Gui();
        var event = new MaidContainerGuiEvent.Init(parent);
        NeoForge.EVENT_BUS.listener.accept(event);
        var button = (net.minecraft.client.gui.components.Button) event.buttons.get("foxmodelloader:model");
        if (button == null) throw new AssertionError("Fox button missing from maid GUI");
        button.click.accept(button);
        var screen = (ModernPlayerModelScreen) InputUtil.screen;
        if (screen == null || screen.parent != parent) throw new AssertionError("Model screen did not open with its parent");
        screen.selection.accept("custom/test", "default");
        var packet = (C2SSetMaidModelPacket) NetworkHandler.sent;
        if (packet == null || packet.entityId() != 42 || !packet.modelId().equals("custom/test") || !packet.textureId().equals("default"))
            throw new AssertionError("Model selection did not use Fox's serverbound packet");
        NeoForge.EVENT_BUS = new NeoForge.Bus();
        System.setProperty("fixture.ysm", "true"); init();
        if (NeoForge.EVENT_BUS.listener != null) throw new AssertionError("Original YSM must own its own integration");
        System.clearProperty("fixture.ysm");
        System.setProperty("fixture.noMaid", "true"); init();
        if (NeoForge.EVENT_BUS.listener != null) throw new AssertionError("Absent optional maid mod registered a listener");
    }
}'''
    }
    return result
