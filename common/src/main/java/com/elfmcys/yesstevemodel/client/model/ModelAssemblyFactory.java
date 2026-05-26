package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.client.ClientModelInfo;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.file.VehicleModelFiles;
import com.elfmcys.yesstevemodel.client.gui.metadata.ModelDisplayAssets;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.client.animation.condition.ArmorConditions;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationControllerFile;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.util.FileTypeUtil;
import com.elfmcys.yesstevemodel.geckolib3.file.ProjectileModelFiles;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ModelAssemblyFactory {

    private static final String FIRST_PERSON_ARM_BONE = "fp_arm";

    private static final String BOAT_ANIMATION = "boat";

    private static final Set<String> BOAT_DYNAMIC_BONES = Set.of(
            "AllBody",
            "LeftArm",
            "LeftForeArm",
            "RightArm",
            "RightForeArm"
    );

    private static ModelAssembly primaryAssembly;

    public static ModelAssembly buildAssembly(ClientModelInfo clientModelInfo, boolean isPrimary, boolean isAuth) {
        ArrayList<AbstractTexture> textureList = new ArrayList();
        ModelResourceBundle resourceBundle = buildResourceBundle(clientModelInfo);
        ModelAssembly assembly = new ModelAssembly(
                buildPlayerModelBundle(clientModelInfo, resourceBundle, isPrimary, textureList),
                buildProjectileModels(clientModelInfo, resourceBundle, isPrimary, textureList),
                buildVehicleModels(clientModelInfo, resourceBundle, isPrimary, textureList),
                resourceBundle, clientModelInfo.getInfo(),
                buildTextureRegistry(clientModelInfo, isAuth, textureList), textureList
        );
        if (isPrimary) {
            primaryAssembly = assembly;
            primaryAssembly.getAnimationBundle().getMainAnimations().values().forEach(animation -> {
                animation.isFromPrimaryAssembly = true;
            });
        }
        return assembly;
    }

    public static PlayerModelBundle buildPlayerModelBundle(ClientModelInfo clientModelInfo, ModelResourceBundle resourceBundle, boolean isPrimary, List<AbstractTexture> textureList) {
        MainModelData hierarchyData = clientModelInfo.getMainModelData();
        GeoModel mainModel = hierarchyData.getModels().get(0);
        GeoModel armModel = hierarchyData.getModels().get(1);
        Object2ReferenceOpenHashMap<String, Animation> object2ReferenceOpenHashMap = new Object2ReferenceOpenHashMap<>();
        Object2ReferenceOpenHashMap<String, Animation> armAnimations = new Object2ReferenceOpenHashMap<>();
        for (String str : hierarchyData.getAnimations().keySet()) {
            AnimationFile animationFile = hierarchyData.getAnimations().get(str);
            if (FIRST_PERSON_ARM_BONE.equals(str)) {
                armAnimations.putAll(animationFile.getAnimations());
            } else {
                object2ReferenceOpenHashMap.putAll(animationFile.getAnimations());
            }
        }
        if (!isPrimary) {
            Animation primaryBoat = primaryAssembly != null ? primaryAssembly.getAnimationBundle().getMainAnimations().get(BOAT_ANIMATION) : null;
            Animation importedBoat = object2ReferenceOpenHashMap.get(BOAT_ANIMATION);
            if (primaryBoat != null && importedBoat != null && importedBoat != primaryBoat) {
                object2ReferenceOpenHashMap.put(BOAT_ANIMATION, mergeBoatPaddleAnimation(importedBoat, primaryBoat));
            }
            ObjectIterator<Map.Entry<String, Animation>> it = primaryAssembly.getAnimationBundle().getMainAnimations().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Animation> entry = it.next();
                object2ReferenceOpenHashMap.computeIfAbsent(entry.getKey(), obj -> {
                    return entry.getValue();
                });
            }
            ObjectIterator<Map.Entry<String, Animation>> it2 = primaryAssembly.getAnimationBundle().getArmAnimations().entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, Animation> entry2 = it2.next();
                armAnimations.computeIfAbsent(entry2.getKey(), obj2 -> {
                    return entry2.getValue();
                });
            }
        }
        ConditionManager conditionManager = new ConditionManager();
        ObjectSet<String> objectSetKeySet = object2ReferenceOpenHashMap.keySet();
        Objects.requireNonNull(conditionManager);
        objectSetKeySet.forEach(conditionManager::addTest);
        ArmorConditions armorRegistry = new ArmorConditions();
        ObjectSet<String> objectSetKeySet2 = armAnimations.keySet();
        Objects.requireNonNull(armorRegistry);
        objectSetKeySet2.forEach(armorRegistry::addCondition);
        Object2ReferenceOpenHashMap<String, AnimationController> animationControllers = new Object2ReferenceOpenHashMap<>();
        for (AnimationControllerFile animationControllerFile : hierarchyData.getAnimationControllers()) {
            animationControllers.putAll(animationControllerFile.getAnimationControllers());
        }
        for (OuterFileTexture texture : hierarchyData.getTextureMap().values()) {
            textureList.add(texture);
            textureList.addAll(texture.getSuffixTextures().values());
        }
        String defaultTextureName;
        if (hierarchyData.getTextureMap().isEmpty()) {
            org.apache.logging.log4j.LogManager.getLogger("yes_steve_model").warn("[YSM] Model has no textures: {}", clientModelInfo.getInfo().getModelProperties().getDefaultTexture());
            return null;
        }
        defaultTextureName = (StringUtils.isEmpty(clientModelInfo.getInfo().getModelProperties().getDefaultTexture()) || !hierarchyData.getTextureMap().containsKey(clientModelInfo.getInfo().getModelProperties().getDefaultTexture())) ? hierarchyData.getTextureMap().getKeyAt(0) : clientModelInfo.getInfo().getModelProperties().getDefaultTexture();
        return new PlayerModelBundle(
                mainModel,
                armModel,
                object2ReferenceOpenHashMap,
                armAnimations,
                conditionManager,
                armorRegistry,
                animationControllers,
                hierarchyData.getTextureMap(),
                defaultTextureName,
                hierarchyData.getTextureMap().get(defaultTextureName),
                resourceBundle);
    }

    private static Animation mergeBoatPaddleAnimation(Animation importedBoat, Animation primaryBoat) {
        if (importedBoat.isEmpty()) {
            return primaryBoat;
        }
        Map<String, BoneAnimation> dynamicBones = new LinkedHashMap<>();
        for (BoneAnimation boneAnimation : primaryBoat.boneAnimations) {
            if (BOAT_DYNAMIC_BONES.contains(boneAnimation.boneName)) {
                dynamicBones.put(boneAnimation.boneName, boneAnimation);
            }
        }
        if (dynamicBones.isEmpty()) {
            return importedBoat;
        }

        ArrayList<BoneAnimation> mergedBones = new ArrayList<>(importedBoat.boneAnimations.size() + dynamicBones.size());
        HashSet<String> mergedNames = new HashSet<>();
        for (BoneAnimation boneAnimation : importedBoat.boneAnimations) {
            BoneAnimation replacement = dynamicBones.get(boneAnimation.boneName);
            if (replacement != null) {
                mergedBones.add(replacement);
                mergedNames.add(replacement.boneName);
            } else {
                mergedBones.add(boneAnimation);
                mergedNames.add(boneAnimation.boneName);
            }
        }
        for (BoneAnimation boneAnimation : dynamicBones.values()) {
            if (mergedNames.add(boneAnimation.boneName)) {
                mergedBones.add(boneAnimation);
            }
        }

        Animation merged = new Animation(
                importedBoat.animationName,
                importedBoat.animationLength,
                importedBoat.loop,
                importedBoat.unKnowData1,
                importedBoat.unKnowData2,
                importedBoat.blendWeight,
                importedBoat.override,
                mergedBones.toArray(new BoneAnimation[0]),
                importedBoat.soundKeyFrames.toArray(new com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame[0]),
                importedBoat.particleKeyFrames.toArray(new com.elfmcys.yesstevemodel.geckolib3.core.event.ParticleEventKeyFrame[0]),
                importedBoat.customInstructionKeyframes.toArray(new com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame[0])
        );
        merged.sourceKey = importedBoat.sourceKey;
        return merged;
    }

    private static Map<Identifier, ProjectileModelBundle> buildProjectileModels(ClientModelInfo clientModelInfo, ModelResourceBundle resourceBundle, boolean isPrimary, List<AbstractTexture> textureList) {
        Object2ReferenceOpenHashMap<Identifier, ProjectileModelBundle> projectileMap = new Object2ReferenceOpenHashMap();
        for (ProjectileModelFiles projectileFiles : clientModelInfo.getExtraItemModels()) {
            GeoModel model = projectileFiles.getModel();
            AnimationFile animationFile = projectileFiles.getAnimations();
            AnimationControllerFile controllerFile = projectileFiles.getAnimationController();
            Object2ReferenceOpenHashMap<String, Animation> animations = new Object2ReferenceOpenHashMap(animationFile != null ? animationFile.getAnimations() : Object2ReferenceMaps.emptyMap());
            Object2ReferenceMap<String, AnimationController> controllers = Object2ReferenceMaps.emptyMap();
            if (controllerFile != null) {
                controllers = new Object2ReferenceOpenHashMap(controllerFile.getAnimationControllers());
            }
            OuterFileTexture texture = projectileFiles.getTexture();
            if (texture != null) {
                textureList.add(texture);
                textureList.addAll(texture.getSuffixTextures().values());
            }
            ProjectileModelBundle projectileBundle = new ProjectileModelBundle(model, animations, controllers, texture, resourceBundle);
            Iterator<Identifier> typeIterator = FileTypeUtil.resolveEntityTypes(projectileFiles.getTextureNames()).iterator();
            while (typeIterator.hasNext()) {
                projectileMap.put(typeIterator.next(), projectileBundle);
            }
        }
        return projectileMap;
    }

    private static Map<Identifier, VehicleModelBundle> buildVehicleModels(ClientModelInfo clientModelInfo, ModelResourceBundle resourceBundle, boolean isPrimary, List<AbstractTexture> textureList) {
        Object2ReferenceOpenHashMap<Identifier, VehicleModelBundle> vehicleMap = new Object2ReferenceOpenHashMap<>();
        for (VehicleModelFiles vehicleFiles : clientModelInfo.getVehicleModelFiles()) {
            GeoModel model = vehicleFiles.getModel();
            AnimationFile animationFile = vehicleFiles.getAnimations();
            AnimationControllerFile controllerFile = vehicleFiles.getAnimationController();
            Object2ReferenceOpenHashMap<String, Animation> animations = new Object2ReferenceOpenHashMap<>(animationFile != null ? animationFile.getAnimations() : Object2ReferenceMaps.emptyMap());
            Object2ReferenceMap<String, AnimationController> controllers = Object2ReferenceMaps.emptyMap();
            if (controllerFile != null) {
                controllers = new Object2ReferenceOpenHashMap<>(controllerFile.getAnimationControllers());
            }
            OuterFileTexture texture = vehicleFiles.getTexture();
            if (texture != null) {
                textureList.add(texture);
                textureList.addAll(texture.getSuffixTextures().values());
            }
            VehicleModelBundle vehicleBundle = new VehicleModelBundle(model, animations, controllers, texture, resourceBundle);
            for (Identifier Identifier : FileTypeUtil.resolveEntityTypes(vehicleFiles.getTextureNames())) {
                vehicleMap.put(Identifier, vehicleBundle);
            }
        }
        if (!isPrimary && primaryAssembly != null) {
            for (Map.Entry<Identifier, VehicleModelBundle> entry : primaryAssembly.getVehicleModels().entrySet()) {
                vehicleMap.computeIfAbsent(entry.getKey(), k -> entry.getValue());
            }
        }
        return vehicleMap;
    }

    private static ModelResourceBundle buildResourceBundle(ClientModelInfo clientModelInfo) {
        return new ModelResourceBundle(clientModelInfo.getExtraResources().getAudioTracks(), buildMolangFunctions(clientModelInfo), extractMolangEvents(clientModelInfo), clientModelInfo.getExtraResources().getTranslations());
    }

    private static ModelDisplayAssets buildTextureRegistry(ClientModelInfo clientModelInfo, boolean isAuth, List<AbstractTexture> textureList) {
        Map<String, AbstractTexture> extraTextures = extractExtraTextures(clientModelInfo, textureList);
        Metadata metadata = clientModelInfo.getInfo().getExtraInfo();
        return new ModelDisplayAssets(metadata != null ? metadata.getName() : StringPool.EMPTY, isAuth, clientModelInfo.getAvatarTextures(), extraTextures);
    }

    private static Object2ReferenceOpenHashMap<String, IValue> buildMolangFunctions(ClientModelInfo clientModelInfo) {
        Object2ReferenceOpenHashMap<String, IValue> functions = new Object2ReferenceOpenHashMap<>(clientModelInfo.getExtraResources().getFunctions().size());
        for (Map.Entry<String, IValue> entry : clientModelInfo.getExtraResources().getFunctions().entrySet()) {
            String key = entry.getKey();
            int atIndex = key.indexOf('@');
            if (atIndex != 0) {
                if (atIndex != -1) {
                    key = key.substring(0, atIndex);
                }
                functions.put(key, entry.getValue());
            }
        }
        return functions;
    }

    private static Object2ReferenceOpenHashMap<String, List<IValue>> extractMolangEvents(ClientModelInfo clientModelInfo) {
        Object2ReferenceOpenHashMap<String, List<IValue>> events = new Object2ReferenceOpenHashMap<>();
        for (Map.Entry<String, IValue> entry : clientModelInfo.getExtraResources().getFunctions().entrySet()) {
            int atIndex = entry.getKey().indexOf('@');
            if (atIndex != -1 && atIndex + 1 < entry.getKey().length()) {
                events.computeIfAbsent(entry.getKey().substring(atIndex + 1).toLowerCase(), obj -> {
                    return new ReferenceArrayList();
                }).add(entry.getValue());
            }
        }
        return events;
    }

    public static Map<String, AbstractTexture> extractExtraTextures(ClientModelInfo clientModelInfo, List<AbstractTexture> textureList) {
        Object2ObjectOpenHashMap<String, AbstractTexture> extraTextures = new Object2ObjectOpenHashMap();
        if (clientModelInfo.getInfo().getModelProperties() != null) {
            for (Map.Entry<String, OuterFileTexture> entry : clientModelInfo.getGuiTextures().entrySet()) {
                OuterFileTexture texture = entry.getValue();
                if (texture != null) {
                    textureList.add(texture);
                    extraTextures.put(entry.getKey(), texture);
                }
            }
        }
        return Object2ObjectMaps.unmodifiable(extraTextures);
    }
}
