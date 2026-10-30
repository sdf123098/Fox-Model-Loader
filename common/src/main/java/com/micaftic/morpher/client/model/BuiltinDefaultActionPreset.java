package com.micaftic.morpher.client.model;

import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.micaftic.morpher.YesSteveModel;
import com.micaftic.morpher.geckolib3.core.builder.Animation;
import com.micaftic.morpher.geckolib3.core.event.ParticleEventKeyFrame;
import com.micaftic.morpher.geckolib3.core.keyframe.BoneAnimation;
import com.micaftic.morpher.geckolib3.core.keyframe.event.EventKeyFrame;
import com.micaftic.morpher.geckolib3.file.AnimationFile;
import com.micaftic.morpher.resource.bundle.AnimationMapper;
import com.micaftic.morpher.resource.YSMFolderDeserializer;
import com.micaftic.morpher.resource.pojo.RawYsmModel;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BuiltinDefaultActionPreset {

    private static final String BASE_PATH = "/assets/foxmodelloader/builtin/default/animations/";
    private static final String EXTERNAL_YSM_BASE_PATH = "/assets/foxmodelloader/builtin/external_ysm/animations/";
    private static final Map<String, String> PLAYER_ANIMATION_FILES = createPlayerAnimationFiles();

    private static volatile Map<String, AnimationFile> playerAnimations;

    private BuiltinDefaultActionPreset() {
    }

    public static Map<String, AnimationFile> playerAnimations() {
        Map<String, AnimationFile> cached = playerAnimations;
        if (cached == null) {
            synchronized (BuiltinDefaultActionPreset.class) {
                cached = playerAnimations;
                if (cached == null) {
                    cached = loadPlayerAnimations();
                    playerAnimations = cached;
                }
            }
        }
        return cached;
    }

    public static Object2ReferenceMap<String, Animation> mainAnimations() {
        Object2ReferenceLinkedOpenHashMap<String, Animation> animations = new Object2ReferenceLinkedOpenHashMap<>();
        for (Map.Entry<String, AnimationFile> entry : playerAnimations().entrySet()) {
            if (!"fp_arm".equals(entry.getKey())) {
                animations.putAll(entry.getValue().getAnimations());
            }
        }
        return Object2ReferenceMaps.unmodifiable(animations);
    }

    public static Object2ReferenceMap<String, Animation> mainAnimations(GeoModel model) {
        boolean hasLeftSword = hasSwordGeometry(model, "LeftSword");
        boolean hasRightSword = hasSwordGeometry(model, "RightSword");
        Object2ReferenceLinkedOpenHashMap<String, Animation> animations = new Object2ReferenceLinkedOpenHashMap<>();
        for (Object2ReferenceMap.Entry<String, Animation> entry : mainAnimations().object2ReferenceEntrySet()) {
            animations.put(entry.getKey(), adaptSwordAnimation(entry.getValue(), hasLeftSword, hasRightSword));
        }
        return Object2ReferenceMaps.unmodifiable(animations);
    }

    private static boolean hasSwordGeometry(GeoModel model, String swordBoneName) {
        if (model == null || model.bakedBones == null) {
            return false;
        }
        List<GeoModel.BakedBone> bones = model.bakedBones;
        for (int i = 0; i < bones.size(); i++) {
            if (bones.get(i).cubes.stream().noneMatch(cube -> !cube.quads.isEmpty())) {
                continue;
            }
            int index = i;
            for (int depth = 0; index >= 0 && index < bones.size() && depth < bones.size(); depth++) {
                GeoModel.BakedBone bone = bones.get(index);
                if (swordBoneName.equals(bone.name)) {
                    return true;
                }
                index = bone.parentIdx;
            }
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Animation adaptSwordAnimation(Animation source, boolean hasLeftSword, boolean hasRightSword) {
        if (!"hold_mainhand:sword".equals(source.animationName)
                && !"hold_offhand:sword".equals(source.animationName)
                && !"swing:sword".equals(source.animationName)) {
            return source;
        }
        List<BoneAnimation> bones = new ArrayList<>(source.boneAnimations.size());
        boolean changed = false;
        for (BoneAnimation bone : source.boneAnimations) {
            // The sample hides the held item while showing its own LeftSword/RightSword mesh.
            // External models without that mesh must keep the inventory sword visible.
            boolean keepHeldSword = (!hasLeftSword && "LeftHandLocator".equals(bone.boneName))
                    || (!hasRightSword && "RightHandLocator".equals(bone.boneName));
            if (keepHeldSword && !bone.scaleKeyFrames.isEmpty()) {
                bones.add(new BoneAnimation(bone.boneName, bone.rotationKeyFrames, bone.positionKeyFrames, List.of()));
                changed = true;
            } else {
                bones.add(bone);
            }
        }
        if (!changed) {
            return source;
        }
        Animation adapted = new Animation(source.animationName, source.animationLength, source.loop,
                source.unKnowData1, source.unKnowData2, source.blendWeight, source.override,
                bones.toArray(new BoneAnimation[0]), source.soundKeyFrames.toArray(new EventKeyFrame[0]),
                source.particleKeyFrames.toArray(new ParticleEventKeyFrame[0]),
                source.customInstructionKeyframes.toArray(new EventKeyFrame[0]));
        adapted.sourceKey = source.sourceKey;
        adapted.isFromPrimaryAssembly = source.isFromPrimaryAssembly;
        return adapted;
    }

    public static Object2ReferenceMap<String, Animation> armAnimations() {
        AnimationFile armFile = playerAnimations().get("fp_arm");
        if (armFile == null) {
            return Object2ReferenceMaps.emptyMap();
        }
        return Object2ReferenceMaps.unmodifiable(new Object2ReferenceLinkedOpenHashMap<>(armFile.getAnimations()));
    }

    private static Map<String, AnimationFile> loadPlayerAnimations() {
        LinkedHashMap<String, AnimationFile> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : PLAYER_ANIMATION_FILES.entrySet()) {
            byte[] data = readResource(resolveResourcePath(entry));
            if (data == null) {
                YesSteveModel.LOGGER.warn("[SM] Missing builtin default action preset file {}", entry.getValue());
                continue;
            }
            try {
                RawYsmModel.RawAnimationFile rawFile = YSMFolderDeserializer.parseAnimationFile(data);
                Map<String, Animation> animations = AnimationMapper.buildAnimations(rawFile, false);
                animations.values().forEach(animation -> {
                    if (animation.sourceKey == null) {
                        animation.sourceKey = entry.getKey();
                    }
                });
                result.put(entry.getKey(), new AnimationFile(animations));
            } catch (Exception e) {
                YesSteveModel.LOGGER.warn("[SM] Failed to load builtin default action preset file {}", entry.getValue(), e);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, String> createPlayerAnimationFiles() {
        LinkedHashMap<String, String> files = new LinkedHashMap<>();
        files.put("main", "main.animation.json");
        files.put("arm", "arm.animation.json");
        files.put("extra", "extra.animation.json");
        files.put("tac", "tac.animation.json");
        files.put("carryon", "carryon.animation.json");
        files.put("parcool", "parcool.animation.json");
        files.put("swem", "swem.animation.json");
        files.put("slashblade", "slashblade.animation.json");
        files.put("tlm", "tlm.animation.json");
        files.put("immersive_melodies", "im.animation.json");
        files.put("irons_spell_books", "iss.animation.json");
        files.put("fp_arm", "fp.arm.animation.json");
        return Collections.unmodifiableMap(files);
    }

    private static String resolveResourcePath(Map.Entry<String, String> entry) {
        if ("main".equals(entry.getKey())) {
            return EXTERNAL_YSM_BASE_PATH + entry.getValue();
        }
        return BASE_PATH + entry.getValue();
    }

    private static byte[] readResource(String path) {
        try (InputStream in = BuiltinDefaultActionPreset.class.getResourceAsStream(path)) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            YesSteveModel.LOGGER.warn("[SM] Failed to read builtin default action preset resource {}", path, e);
            return null;
        }
    }
}
