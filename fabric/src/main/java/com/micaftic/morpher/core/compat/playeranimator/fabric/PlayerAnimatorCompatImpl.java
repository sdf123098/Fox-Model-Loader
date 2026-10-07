package com.micaftic.morpher.core.compat.playeranimator.fabric;

import net.minecraft.client.player.AbstractClientPlayer;

public final class PlayerAnimatorCompatImpl {

    private PlayerAnimatorCompatImpl() {
    }

    public static boolean isLoaded() {
        return hasClass("dev.kosmx.playerAnim.api.IPlayer")
                || hasClass("com.zigythebird.playeranim.api.PlayerAnimationAccess");
    }

    public static boolean isPlayerAnimated(AbstractClientPlayer abstractClientPlayer) {
        if (abstractClientPlayer == null) {
            return false;
        }
        if (hasClass("net.bettercombat.client.animation.AttackAnimationStack")) {
            return isBetterCombatAttackActive(abstractClientPlayer);
        }
        if (hasClass("net.bettercombat.client.animation.AttackAnimationSubStack")) {
            return isLegacyBetterCombatAttackActive(abstractClientPlayer);
        }
        if (!isLoaded()) {
            return false;
        }
        for (String methodName : new String[] { "getAnimationStack", "playerAnimator_getAnimation", "getAnimation" }) {
            try {
                Object animation = abstractClientPlayer.getClass().getMethod(methodName).invoke(abstractClientPlayer);
                if (isActive(animation)) {
                    return true;
                }
            } catch (ReflectiveOperationException ignored) {
                // Try the next Player Animator API name supported by this version.
            }
        }
        return false;
    }

    private static boolean isBetterCombatAttackActive(AbstractClientPlayer player) {
        try {
            ClassLoader loader = PlayerAnimatorCompatImpl.class.getClassLoader();
            Class<?> stackType = Class.forName("net.bettercombat.client.animation.AttackAnimationStack", false, loader);
            Object id = stackType.getField("ID").get(null);
            Class<?> accessType = Class.forName("com.zigythebird.playeranim.api.PlayerAnimationAccess", false, loader);
            for (java.lang.reflect.Method method : accessType.getMethods()) {
                if (method.getName().equals("getPlayerAnimationLayer") && method.getParameterCount() == 2
                        && method.getParameterTypes()[0].isInstance(player)
                        && method.getParameterTypes()[1].isInstance(id)) {
                    return isActive(method.invoke(null, player, id));
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Better Combat's current attack layer is unavailable; fail closed.
        }
        return false;
    }

    private static boolean isLegacyBetterCombatAttackActive(AbstractClientPlayer player) {
        try {
            java.lang.reflect.Field attack = findField(player.getClass(), "attackAnimation");
            if (attack == null) {
                return false;
            }
            if (!attack.trySetAccessible()) {
                return false;
            }
            Object subStack = attack.get(player);
            java.lang.reflect.Field base = findField(subStack.getClass(), "base");
            if (base == null || !base.trySetAccessible()) {
                return false;
            }
            return isActive(base.get(subStack));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static java.lang.reflect.Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Mixin fields can live on AbstractClientPlayer while the runtime player is a subclass.
            }
        }
        return null;
    }

    private static boolean isActive(Object animation) throws ReflectiveOperationException {
        return animation != null && Boolean.TRUE.equals(animation.getClass().getMethod("isActive").invoke(animation));
    }

    private static boolean hasClass(String name) {
        try {
            Class.forName(name, false, PlayerAnimatorCompatImpl.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
