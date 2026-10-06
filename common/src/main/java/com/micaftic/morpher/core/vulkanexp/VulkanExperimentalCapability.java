package com.micaftic.morpher.core.vulkanexp;

import com.micaftic.morpher.core.render.SmGraphicsBackendDetector;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;

public final class VulkanExperimentalCapability {
    private VulkanExperimentalCapability() {
    }

    public static Report probe() {
        try {
            GpuDevice gpuDevice = RenderSystem.getDevice();
            if (gpuDevice == null) {
                return Report.disabled("RenderSystem device is null");
            }

            String backendName = gpuDevice.getDeviceInfo().backendName();
            return Report.disabled("26.3 Renderpearl public API reports backend '" + backendName
                    + "' but does not expose Vulkan native handles for this experimental probe");
        } catch (Throwable t) {
            return Report.disabled("Vulkan capability probe failed: " + t.getClass().getSimpleName()
                    + ": " + String.valueOf(t.getMessage()));
        }
    }

    public record Report(
            boolean enabled,
            String reason,
            String gpuDeviceClass,
            String deviceBackendClass,
            String commandBackendClass,
            boolean vkDeviceAvailable,
            boolean computeQueueAvailable,
            int computeQueueFamilyIndex,
            boolean vmaAvailable
    ) {
        public static Report disabled(String reason) {
            return new Report(false, reason, "unknown", "unknown", "unknown", false, false, -1, false);
        }

        public String summary() {
            return "enabled=" + enabled
                    + ", reason=" + reason
                    + ", gpuDeviceClass=" + gpuDeviceClass
                    + ", deviceBackendClass=" + deviceBackendClass
                    + ", commandBackendClass=" + commandBackendClass
                    + ", vkDeviceAvailable=" + vkDeviceAvailable
                    + ", computeQueueAvailable=" + computeQueueAvailable
                    + ", computeQueueFamilyIndex=" + computeQueueFamilyIndex
                    + ", vmaAvailable=" + vmaAvailable;
        }
    }
}
