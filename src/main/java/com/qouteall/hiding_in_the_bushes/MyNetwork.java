package com.qouteall.hiding_in_the_bushes;

import com.qouteall.hiding_in_the_bushes.network.NetworkMain;
import com.qouteall.hiding_in_the_bushes.network.StcDimensionConfirm;
import com.qouteall.hiding_in_the_bushes.network.StcRedirected;
import com.qouteall.hiding_in_the_bushes.network.StcSpawnEntity;
import com.qouteall.hiding_in_the_bushes.network.StcUpdateGlobalPortals;
import com.qouteall.immersive_portals.my_util.ICustomStcPacket;
import com.qouteall.immersive_portals.portal.global_portals.GlobalPortalStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MyNetwork {
    public static void init() {
        NetworkMain.init();
    }
    
    public static IPacket createRedirectedMessage(
        DimensionType dimension,
        IPacket packet
    ) {
        return NetworkMain.channel.toVanillaPacket(
            new StcRedirected(dimension, packet),
            NetworkDirection.PLAY_TO_CLIENT
        );
    }
    
    public static IPacket createStcDimensionConfirm(
        DimensionType dimensionType,
        Vec3d pos
    ) {
        return NetworkMain.channel.toVanillaPacket(
            new StcDimensionConfirm(dimensionType, pos),
            NetworkDirection.PLAY_TO_CLIENT
        );
    }
    
    //NOTE my packet is redirected but I cannot get the packet handler info here
    public static IPacket createStcSpawnEntity(
        Entity entity
    ) {
        CompoundNBT tag = new CompoundNBT();
        entity.writeWithoutTypeId(tag);
        return NetworkMain.channel.toVanillaPacket(
            new StcSpawnEntity(
                EntityType.getKey(entity.getType()).toString(),
                entity.getEntityId(),
                entity.world.dimension.getType(),
                tag
            ),
            NetworkDirection.PLAY_TO_CLIENT
        );
    }
    
    public static IPacket createGlobalPortalUpdate(
        GlobalPortalStorage storage
    ) {
        return NetworkMain.channel.toVanillaPacket(
            new StcUpdateGlobalPortals(
                storage.write(new CompoundNBT()),
                storage.world.get().dimension.getType()
            ),
            NetworkDirection.PLAY_TO_CLIENT
        );
    }
    
    public static void sendRedirectedMessage(
        ServerPlayerEntity player,
        DimensionType dimension,
        IPacket packet
    ) {
        player.connection.sendPacket(createRedirectedMessage(dimension, packet));
    }
}
