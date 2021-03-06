package com.qouteall.immersive_portals.mixin_client;

import com.qouteall.immersive_portals.CGlobal;
import com.qouteall.immersive_portals.chunk_loading.MyClientChunkManager;
import com.qouteall.immersive_portals.ducks.IEClientWorld;
import com.qouteall.immersive_portals.ducks.IEWorld;
import com.qouteall.immersive_portals.portal.global_portals.GlobalTrackedPortal;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements IEClientWorld {
    @Shadow
    @Final
    @Mutable
    private ClientPlayNetHandler connection;
    
    @Shadow
    public abstract Entity getEntityByID(int id);
    
    private List<GlobalTrackedPortal> globalTrackedPortals;
    
    @Override
    public ClientPlayNetHandler getNetHandler() {
        return connection;
    }
    
    @Override
    public void setNetHandler(ClientPlayNetHandler handler) {
        connection = handler;
    }
    
    @Override
    public List<GlobalTrackedPortal> getGlobalPortals() {
        return globalTrackedPortals;
    }
    
    @Override
    public void setGlobalPortals(List<GlobalTrackedPortal> arg) {
        globalTrackedPortals = arg;
    }
    
    //use my client chunk manager
    @Inject(
        method = "Lnet/minecraft/client/world/ClientWorld;<init>(Lnet/minecraft/client/network/play/ClientPlayNetHandler;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/world/dimension/DimensionType;ILnet/minecraft/profiler/IProfiler;Lnet/minecraft/client/renderer/WorldRenderer;)V",
        at = @At("RETURN")
    )
    void onConstructed(
        ClientPlayNetHandler clientPlayNetworkHandler_1,
        WorldSettings levelInfo_1,
        DimensionType dimensionType_1,
        int int_1,
        IProfiler profiler_1,
        WorldRenderer worldRenderer_1,
        CallbackInfo ci
    ) {
        ClientWorld clientWorld = (ClientWorld) (Object) this;
        MyClientChunkManager chunkManager = new MyClientChunkManager(clientWorld, int_1);
        ((IEWorld) this).setChunkManager(chunkManager);
    }
    
    //avoid entity duplicate when an entity travels
    @Inject(
        method = "Lnet/minecraft/client/world/ClientWorld;addEntityImpl(ILnet/minecraft/entity/Entity;)V",
        at = @At("TAIL")
    )
    private void onOnEntityAdded(int entityId, Entity entityIn, CallbackInfo ci) {
        CGlobal.clientWorldLoader.clientWorldMap.values().stream()
            .filter(world -> world != (Object) this)
            .forEach(world -> world.removeEntityFromWorld(entityId));
    }

}
