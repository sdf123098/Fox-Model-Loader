"""Run actual cache/render-gate code against Tweakeroo's detached-camera contract.

The camera shares the player's identity and may temporarily spoof spectator
status. These fixtures do not replace a real Minecraft visual test.
"""
from pathlib import Path
import sys
import unittest

sys.dont_write_bytecode = True
from test_mod_id_compat import run_fixture

ROOT = Path(__file__).resolve().parents[2]
FABRIC = (ROOT / 'fabric').is_dir()
EVENT_SOURCE = (ROOT / 'common/src/main/java/com/micaftic/morpher/client/event/ReplacePlayerRenderEvent.java').read_text(encoding='utf-8')
CLASSIC = 'SubmitNodeCollector' not in EVENT_SOURCE
STORE_PACKAGE = 'com.micaftic.morpher.capability.' + ('fabric.client' if FABRIC else 'client')
BUFFER = ('com.micaftic.morpher.client.renderer.MultiBufferSource'
          if 'import com.micaftic.morpher.client.renderer.MultiBufferSource;' in EVENT_SOURCE
          else 'net.minecraft.client.renderer.MultiBufferSource')


def sources(fixture):
    store_path = STORE_PACKAGE.replace('.', '/') + '/PlayerCapabilityClientStore.java'
    base = ROOT / ('fabric/src/main/java' if FABRIC else 'common/src/main/java')
    result = {
        store_path: (base / store_path).read_text(encoding='utf-8'),
        'com/micaftic/morpher/client/event/ReplacePlayerRenderEvent.java':
            (ROOT / 'common/src/main/java/com/micaftic/morpher/client/event/ReplacePlayerRenderEvent.java').read_text(encoding='utf-8'),
        'com/micaftic/morpher/client/render/PlayerRenderPolicy.java':
            (ROOT / 'common/src/main/java/com/micaftic/morpher/client/render/PlayerRenderPolicy.java').read_text(encoding='utf-8'),
        'Fixture.java': fixture,
        'net/minecraft/world/entity/Entity.java': '''package net.minecraft.world.entity;
public class Entity {
  public java.util.UUID uuid; public int id;
  public java.util.UUID getUUID(){return uuid;} public int getId(){return id;}
}''',
        'net/minecraft/world/entity/player/Player.java': '''package net.minecraft.world.entity.player;
public class Player extends net.minecraft.world.entity.Entity {
  public boolean spectator; public float getYRot(){return 0;}
  public boolean isSpectator(){return spectator;}
  public net.minecraft.network.chat.Component getName(){return new net.minecraft.network.chat.Component();}
}''',
        'net/minecraft/client/player/AbstractClientPlayer.java': 'package net.minecraft.client.player; public class AbstractClientPlayer extends net.minecraft.world.entity.player.Player {}',
        'net/minecraft/client/player/LocalPlayer.java': 'package net.minecraft.client.player; public class LocalPlayer extends AbstractClientPlayer {}',
        'fi/dy/masa/tweakeroo/util/CameraEntity.java': '''package fi.dy.masa.tweakeroo.util;
public class CameraEntity extends net.minecraft.client.player.LocalPlayer {
  public CameraEntity(net.minecraft.client.player.LocalPlayer body){uuid=body.uuid; id=body.id; spectator=true;}
}''',
        'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
public class Minecraft {
  private static final Minecraft INSTANCE=new Minecraft();
  public net.minecraft.client.player.LocalPlayer player;
  public net.minecraft.world.entity.Entity camera;
  public net.minecraft.client.multiplayer.ClientLevel level=new net.minecraft.client.multiplayer.ClientLevel();
  public net.minecraft.client.multiplayer.MultiPlayerGameMode gameMode=new net.minecraft.client.multiplayer.MultiPlayerGameMode();
  public static Minecraft getInstance(){return INSTANCE;}
  public net.minecraft.world.entity.Entity getCameraEntity(){return camera;}
}''',
        'net/minecraft/client/multiplayer/ClientLevel.java': '''package net.minecraft.client.multiplayer;
public class ClientLevel {
  public net.minecraft.world.entity.Entity entity;
  public net.minecraft.world.entity.Entity getEntity(int id){return entity;}
}''',
        'net/minecraft/client/multiplayer/MultiPlayerGameMode.java': '''package net.minecraft.client.multiplayer;
public class MultiPlayerGameMode {
  public net.minecraft.world.level.GameType mode=net.minecraft.world.level.GameType.SURVIVAL;
  public net.minecraft.world.level.GameType getPlayerMode(){return mode;}
}''',
        'net/minecraft/world/level/GameType.java': 'package net.minecraft.world.level; public enum GameType { SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR }',
        'net/minecraft/network/chat/Component.java': 'package net.minecraft.network.chat; public class Component { public String getString(){return "fixture";} }',
        'com/micaftic/morpher/capability/PlayerCapability.java': f'''package com.micaftic.morpher.capability;
public class PlayerCapability {{
  public net.minecraft.world.entity.player.Player entity; public String model="";
  public PlayerCapability(net.minecraft.world.entity.player.Player player){{entity=player;}}
  public static java.util.Optional<PlayerCapability> get(net.minecraft.world.entity.player.Player player){{return {STORE_PACKAGE}.PlayerCapabilityClientStore.get(player);}}
  public boolean isModelActive(){{return !model.isEmpty();}} public boolean hasRenderableModel(){{return isModelActive();}}
  public boolean isModelReady(){{return isModelActive();}} public String getModelId(){{return model;}}
}}''',
        'com/micaftic/morpher/YesSteveModel.java': '''package com.micaftic.morpher;
public class YesSteveModel {
  public static boolean isAvailable(){return true;} public static final Log LOGGER=new Log();
  public static class Log { public void info(String text,Object... args){} public void warn(String text,Object... args){} }
}''',
        'com/micaftic/morpher/util/CameraUtil.java': '''package com.micaftic.morpher.util;
public class CameraUtil { public static boolean isFirstPerson(Object model){return false;} }''',
        'com/micaftic/morpher/client/renderer/ModelPreviewRenderer.java': '''package com.micaftic.morpher.client.renderer;
public class ModelPreviewRenderer { public static final float FRONT_FACING_YAW=180; public static boolean isInventoryPreviewFrontFacing(){return false;} }''',
        'com/micaftic/morpher/client/renderer/RendererManager.java': '''package com.micaftic.morpher.client.renderer;
public class RendererManager {
  public static int renders; private static final Renderer RENDERER=new Renderer();
  public static Renderer getPlayerRenderer(){return RENDERER;}
  public static class Renderer { public void render(com.micaftic.morpher.capability.PlayerCapability cap,Object... args){renders++;} }
}''',
        'com/micaftic/morpher/core/config/ConfigPolicies.java': '''package com.micaftic.morpher.core.config;
public class ConfigPolicies {
  public static boolean disableSelf,disableOthers;
  public static Render render(){return new Render();} public static Diagnostics diagnostics(){return new Diagnostics();}
  public static class Diagnostics { public boolean animationDebugLog(){return false;} }
  public static class Render {
    public boolean disableSelfModel(){return disableSelf;} public boolean disableOtherModel(){return disableOthers;}
    public boolean disableExternalFirstPersonAnimation(){return false;}
  }
}''',
        'com/micaftic/morpher/core/compat/firstperson/FirstPersonCompat.java': 'package com.micaftic.morpher.core.compat.firstperson; public class FirstPersonCompat { public static boolean isFirstPersonActive(){return false;} }',
        'com/micaftic/morpher/core/compat/playeranimator/PlayerAnimatorCompat.java': 'package com.micaftic.morpher.core.compat.playeranimator; public class PlayerAnimatorCompat { public static boolean isPlayerAnimated(Object player){return false;} }',
        'com/micaftic/morpher/core/compat/realcamera/RealCameraCompat.java': 'package com.micaftic.morpher.core.compat.realcamera; public class RealCameraCompat { public static boolean isActive(){return false;} }',
        'com/mojang/blaze3d/vertex/PoseStack.java': 'package com.mojang.blaze3d.vertex; public class PoseStack {}',
        'net/minecraft/client/renderer/MultiBufferSource.java': 'package net.minecraft.client.renderer; public class MultiBufferSource {}',
        'com/micaftic/morpher/client/renderer/MultiBufferSource.java': 'package com.micaftic.morpher.client.renderer; public class MultiBufferSource {}',
        'net/minecraft/client/renderer/SubmitNodeCollector.java': 'package net.minecraft.client.renderer; public class SubmitNodeCollector {}',
    }
    helper = 'com/micaftic/morpher/client/compat/TweakerooCompat.java'
    if (ROOT / 'common/src/main/java' / helper).is_file():
        result[helper] = (ROOT / 'common/src/main/java' / helper).read_text(encoding='utf-8')
    return result


class TweakerooFreeCameraTest(unittest.TestCase):
    def test_camera_cannot_replace_body_model_cache(self):
        run_fixture(sources(f'''import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.micaftic.morpher.capability.PlayerCapability;
public class Fixture {{
  public static void main(String[] args) {{
    Minecraft mc=Minecraft.getInstance(); LocalPlayer body=new LocalPlayer();
    body.uuid=java.util.UUID.randomUUID(); body.id=42; mc.player=body; mc.camera=body;
    PlayerCapability selected=PlayerCapability.get(body).orElseThrow(); selected.model="fox-selected-model";
    var camera=new fi.dy.masa.tweakeroo.util.CameraEntity(body); mc.camera=camera;
    if(PlayerCapability.get(camera).isPresent()) throw new AssertionError("Camera proxy must not own a player model cache");
    if(PlayerCapability.get(body).orElseThrow()!=selected) throw new AssertionError("Body cache replaced by camera");
    mc.camera=body;
    if(!PlayerCapability.get(body).orElseThrow().model.equals("fox-selected-model")) throw new AssertionError("Model lost on exit");
    LocalPlayer respawn=new LocalPlayer(); respawn.uuid=body.uuid; respawn.id=43; mc.player=respawn;
    if(PlayerCapability.get(respawn).orElseThrow()==selected) throw new AssertionError("Respawn reused stale entity");
    {STORE_PACKAGE}.PlayerCapabilityClientStore.clear();
    if(PlayerCapability.get(respawn).orElseThrow().isModelActive()) throw new AssertionError("Disconnect did not clear state");
  }}
}}'''))

    def test_spoofed_spectator_body_renders_custom_but_real_spectator_does_not(self):
        render_args = 'player, 0f, 0f, pose, buffer, 123' if CLASSIC else 'player, 0f, 0f, pose, buffer, null, 123'
        run_fixture(sources(f'''import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import com.micaftic.morpher.capability.PlayerCapability;
import com.micaftic.morpher.client.event.ReplacePlayerRenderEvent;
import com.micaftic.morpher.core.config.ConfigPolicies;
public class Fixture {{
  static boolean render(Player player) {{
    var pose=new com.mojang.blaze3d.vertex.PoseStack(); var buffer=new {BUFFER}();
    com.micaftic.morpher.client.renderer.RendererManager.renders=0;
    boolean replaced=ReplacePlayerRenderEvent.onRenderPlayerPre({render_args});
    if(replaced && com.micaftic.morpher.client.renderer.RendererManager.renders!=1)
      throw new AssertionError("Custom render gate passed without rendering the model");
    return replaced;
  }}
  public static void main(String[] args) {{
    Minecraft mc=Minecraft.getInstance(); LocalPlayer body=new LocalPlayer();
    body.uuid=java.util.UUID.randomUUID(); body.id=42; mc.player=body; mc.camera=body;
    PlayerCapability.get(body).orElseThrow().model="fox-selected-model";
    if(!render(body)) throw new AssertionError("Normal body rendering");
    mc.camera=new fi.dy.masa.tweakeroo.util.CameraEntity(body); body.spectator=true;
    if(!render(body)) throw new AssertionError("Freecam spectator spoof fell back to vanilla skin");
    mc.gameMode.mode=GameType.SPECTATOR;
    if(render(body)) throw new AssertionError("Actual spectator became visible");
    mc.gameMode.mode=GameType.SURVIVAL; ConfigPolicies.disableSelf=true;
    if(render(body)) throw new AssertionError("Disabled self model ignored");
    ConfigPolicies.disableSelf=false;
    var other=new net.minecraft.client.player.AbstractClientPlayer(); other.uuid=java.util.UUID.randomUUID();
    PlayerCapability.get(other).orElseThrow().model="remote-model"; other.spectator=true;
    if(render(other)) throw new AssertionError("Remote spectator ignored");
    other.spectator=false; if(!render(other)) throw new AssertionError("Other custom model blocked");
    ConfigPolicies.disableOthers=true; if(render(other)) throw new AssertionError("Other model setting ignored");
    mc.camera=body;
    if(render(body)) throw new AssertionError("Spectator exception escaped freecam");
    body.spectator=false;
    if(!render(body)) throw new AssertionError("Normal rendering did not recover after freecam");
  }}
}}'''))


if __name__ == '__main__':
    unittest.main()
