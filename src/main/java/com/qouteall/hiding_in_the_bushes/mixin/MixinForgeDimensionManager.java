package com.qouteall.hiding_in_the_bushes.mixin;

import com.qouteall.hiding_in_the_bushes.DimensionSyncManager;
import com.qouteall.hiding_in_the_bushes.ModMainForge;
import com.qouteall.hiding_in_the_bushes.alternate_dimension.AlternateDimensionEntry;
import com.qouteall.immersive_portals.chunk_loading.NewChunkTrackingGraph;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DimensionManager.class, remap = false)
public class MixinForgeDimensionManager {
    /**
     * @author qouteall
     * @reason Forge's dimension unloading mechanic creates many problems
     *   and has literally no use.
     *  Loading a dimension without any chunk loaded won't affect performance.
     */
    @Overwrite
    public static void unloadWorlds(MinecraftServer server, boolean checkLeaks){
    
    }
    
    @Inject(
        method = "registerDimension",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void onDimensionRegistered(
        ResourceLocation name,
        ModDimension type,
        PacketBuffer data,
        boolean hasSkyLight,
        CallbackInfoReturnable<DimensionType> cir
    ) {
        DimensionType newDimensionType = cir.getReturnValue();
        DimensionSyncManager.onDimensionRegisteredAtRuntimeAtServer(newDimensionType);
    }
    
    @Inject(
        method = "readRegistry",
        at = @At("HEAD")
    )
    private static void onStartReadingRegistry(CompoundNBT data, CallbackInfo ci) {
        DimensionSyncManager.beforeServerReadDimensionRegistry();
    }
    
    @Inject(
        method = "readRegistry",
        at = @At("RETURN")
    )
    private static void onAfterReadingRegistry(CompoundNBT data, CallbackInfo ci) {
        DimensionSyncManager.afterServerReadDimensionRegistry();
    }
}
