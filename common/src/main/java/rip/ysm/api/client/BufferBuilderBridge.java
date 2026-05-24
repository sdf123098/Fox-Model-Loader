package rip.ysm.api.client;

import com.mojang.blaze3d.vertex.BufferBuilder;

import java.nio.ByteBuffer;

public final class BufferBuilderBridge {

    private BufferBuilderBridge() {
    }

    public static boolean putBulkData(BufferBuilder builder, ByteBuffer buffer) {
        return rip.ysm.api.client.fabric.BufferBuilderBridgeImpl.putBulkData(builder, buffer);
    }

    public static boolean supportsDirectTransfer() {
        return rip.ysm.api.client.fabric.BufferBuilderBridgeImpl.supportsDirectTransfer();
    }
}
