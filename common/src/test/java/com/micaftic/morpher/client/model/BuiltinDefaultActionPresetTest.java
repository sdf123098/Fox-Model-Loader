package com.micaftic.morpher.client.model;

import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.micaftic.morpher.geckolib3.core.builder.Animation;
import com.micaftic.morpher.geckolib3.core.keyframe.BoneAnimation;
import com.micaftic.morpher.geckolib3.geo.render.built.GeoBone;
import com.micaftic.morpher.resource.bundle.GeometryBaker;
import com.micaftic.morpher.resource.models.GeometryDescription;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltinDefaultActionPresetTest {

    @Test
    void missingSwordGeometryKeepsBothHeldSwordsVisible() {
        var animations = BuiltinDefaultActionPreset.mainAnimations(model());
        assertTrue(bone(animations.get("hold_mainhand:sword"), "RightHandLocator").scaleKeyFrames.isEmpty());
        assertTrue(bone(animations.get("hold_offhand:sword"), "LeftHandLocator").scaleKeyFrames.isEmpty());
        assertTrue(bone(animations.get("swing:sword"), "RightHandLocator").scaleKeyFrames.isEmpty());

        Animation original = BuiltinDefaultActionPreset.mainAnimations().get("swing:sword");
        Animation adapted = animations.get("swing:sword");
        assertSame(bone(original, "RightArm"), bone(adapted, "RightArm"), "keep the sword swing motion");
        assertEquals(original.animationLength, adapted.animationLength);
        assertSame(original.loop, adapted.loop);
        assertEquals(original.sourceKey, adapted.sourceKey);
        assertFalse(bone(original, "RightHandLocator").scaleKeyFrames.isEmpty(), "shared preset must stay intact");
    }

    @Test
    void swordMeshInChildBoneKeepsOnlyThatHandsItemHidden() {
        var mesh = model(bakedBone("RightSword", -1, false), bakedBone("edge", 0, true));
        var animations = BuiltinDefaultActionPreset.mainAnimations(mesh);
        assertSame(BuiltinDefaultActionPreset.mainAnimations().get("swing:sword"), animations.get("swing:sword"));
        assertFalse(bone(animations.get("hold_mainhand:sword"), "RightHandLocator").scaleKeyFrames.isEmpty());
        assertTrue(bone(animations.get("hold_offhand:sword"), "LeftHandLocator").scaleKeyFrames.isEmpty());
    }

    @Test
    void emptySwordBoneAndUnrelatedMeshDoNotReplaceHeldSword() {
        var animations = BuiltinDefaultActionPreset.mainAnimations(model(
                bakedBone("RightSword", -1, false), bakedBone("RightHand", -1, true)));
        assertTrue(bone(animations.get("swing:sword"), "RightHandLocator").scaleKeyFrames.isEmpty());
    }

    @Test
    void leftSwordMeshKeepsOffhandItemHidden() {
        var animations = BuiltinDefaultActionPreset.mainAnimations(model(bakedBone("LeftSword", -1, true)));
        assertFalse(bone(animations.get("hold_offhand:sword"), "LeftHandLocator").scaleKeyFrames.isEmpty());
        assertTrue(bone(animations.get("hold_mainhand:sword"), "RightHandLocator").scaleKeyFrames.isEmpty());
    }

    @Test
    void authoredSwordHidingAndCarryOnHidingArePreserved() {
        var originals = BuiltinDefaultActionPreset.mainAnimations();
        var main = new Object2ReferenceLinkedOpenHashMap<String, Animation>();
        Animation authoredHold = originals.get("hold_mainhand:sword");
        Animation authoredSwing = originals.get("swing:sword");
        main.put("hold_mainhand:sword", authoredHold);
        main.put("swing:sword", authoredSwing);
        ModelAssemblyFactory.applyBuiltinDefaultActionPreset(main, new Object2ReferenceLinkedOpenHashMap<>(), model());

        assertSame(authoredHold, main.get("hold_mainhand:sword"));
        assertSame(authoredSwing, main.get("swing:sword"));
        assertFalse(bone(authoredSwing, "RightHandLocator").scaleKeyFrames.isEmpty());
        assertSame(originals.get("carryon:block"), main.get("carryon:block"));
        assertFalse(bone(main.get("carryon:block"), "RightHandLocator").scaleKeyFrames.isEmpty());
    }

    private static BoneAnimation bone(Animation animation, String name) {
        assertNotNull(animation);
        return animation.boneAnimations.stream().filter(bone -> name.equals(bone.boneName)).findFirst().orElseThrow();
    }

    private static GeoModel model(GeoModel.BakedBone... bones) {
        var model = GeometryBaker.buildMesh(new GeoBone[0], Map.of(),
                new GeometryDescription("geometry.sword-test", 16, 16, 2, 2, new double[]{0, 0, 0}),
                new boolean[]{false});
        model.bakedBones = List.of(bones);
        return model;
    }

    private static GeoModel.BakedBone bakedBone(String name, int parentIndex, boolean drawable) {
        var bone = new GeoModel.BakedBone();
        bone.name = name;
        bone.parentIdx = parentIndex;
        if (drawable) {
            var cube = new GeoModel.BakedCube();
            cube.quads.add(new GeoModel.BakedQuad());
            bone.cubes.add(cube);
        }
        return bone;
    }
}
