package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import rip.ysm.api.network.PacketContext;

public class S2CVersionCheckPacket {

    private final String version;

    public S2CVersionCheckPacket() {
        this(NetworkHandler.VERSION);
    }

    private S2CVersionCheckPacket(String version) {
        this.version = version;
    }

    public static S2CVersionCheckPacket decode(FriendlyByteBuf buf) {
        String version = buf.readUtf();
        if (buf.readableBytes() == 1) {
            ClientModelManager.setAllowUpload(buf.readBoolean());
        } else if (buf.readableBytes() > 1) {
            int readerIndex = buf.readerIndex();
            try {
                String brand = buf.readUtf();
                if ("open_ysm:v1".equals(brand)) {
                    ClientModelManager.setOysmServer(true);
                    if (buf.readableBytes() > 0) {
                        ClientModelManager.setAllowUpload(buf.readBoolean());
                    }
                }
            } catch (RuntimeException ignored) {
                buf.readerIndex(readerIndex);
                if (buf.readableBytes() > 0) {
                    ClientModelManager.setAllowUpload(buf.readBoolean());
                }
            }
        }
        return new S2CVersionCheckPacket(version);
    }

    public static void encode(S2CVersionCheckPacket message, FriendlyByteBuf buf) {
        buf.writeUtf(message.version);
    }

    public static void handle(S2CVersionCheckPacket message, PacketContext ctx) {
        if (NetworkHandler.setChannelVersion(ctx.getConnection(), message.version)) {
            ctx.enqueueWork(ClientModelManager::onSyncConnected);
        }
        if (NetworkHandler.VERSION.equals(message.version)) {
            NetworkHandler.markClientHandshakeComplete();
        }
        ctx.enqueueWork(() -> NetworkHandler.sendToServer(new C2SVersionCheckPacket()));
    }
}
