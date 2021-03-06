package net.dungeonsworkshop.dungeonmaster.common.network;

import net.dungeonsworkshop.dungeonmaster.DungeonMaster;
import net.dungeonsworkshop.dungeonmaster.common.network.handler.ClientMessageHandler;
import net.dungeonsworkshop.dungeonmaster.common.network.handler.MessageHandler;
import net.dungeonsworkshop.dungeonmaster.common.network.handler.ServerMessageHandler;
import net.dungeonsworkshop.dungeonmaster.common.network.message.DisplayScreenMessage;
import net.dungeonsworkshop.dungeonmaster.common.network.message.TileManagerHandleMessage;
import net.dungeonsworkshop.dungeonmaster.common.network.message.TileManagerSyncMessage;
import net.dungeonsworkshop.dungeonmaster.common.network.message.TileManagerLoadMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DungeonsMessageHandler {

    public static final String VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(DungeonMaster.MOD_ID, "instance"), () -> VERSION, VERSION::equals, VERSION::equals);

    private static int index;

    public static void init() {
        registerMessage(DisplayScreenMessage.class, DisplayScreenMessage::encode, DisplayScreenMessage::decode, (msg, ctx) -> getHandler(ctx).handleOpenGuiMessage(msg, ctx));
        registerMessage(TileManagerLoadMessage.class, TileManagerLoadMessage::encode, TileManagerLoadMessage::decode, (msg, ctx) -> getHandler(ctx).handleTileBlockLoadMessage(msg, ctx));
        registerMessage(TileManagerSyncMessage.class, TileManagerSyncMessage::encode, TileManagerSyncMessage::decode, (msg, ctx) -> getHandler(ctx).handleSyncTileManagerMessage(msg, ctx));
        registerMessage(TileManagerHandleMessage.class, TileManagerHandleMessage::encode, TileManagerHandleMessage::decode, (msg, ctx) -> getHandler(ctx).handleSpawnTileMessage(msg, ctx));
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        INSTANCE.registerMessage(index++, messageType, encoder, decoder, messageConsumer);
    }

    private static MessageHandler getHandler(Supplier<NetworkEvent.Context> ctx) {
        return ctx.get().getDirection().getReceptionSide().isClient() ? ClientMessageHandler.INSTANCE : ServerMessageHandler.INSTANCE;
    }


}
