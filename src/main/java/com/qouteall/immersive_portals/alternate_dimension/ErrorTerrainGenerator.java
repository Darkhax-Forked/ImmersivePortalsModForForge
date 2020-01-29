package com.qouteall.immersive_portals.alternate_dimension;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.qouteall.immersive_portals.Helper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.EndChunkGenerator;
import net.minecraft.world.gen.EndGenerationSettings;
import net.minecraft.world.gen.Heightmap;

import java.util.concurrent.TimeUnit;

public class ErrorTerrainGenerator extends EndChunkGenerator {
    private final BlockState AIR;
    
    public static final int regionChunkNum = 4;
    public static final int averageY = 64;
    public static final int maxY = 128;
    
    LoadingCache<ChunkPos, RegionErrorTerrainGenerator> cache = CacheBuilder.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .build(
            new CacheLoader<ChunkPos, RegionErrorTerrainGenerator>() {
                public RegionErrorTerrainGenerator load(ChunkPos key) {
                    return new RegionErrorTerrainGenerator(key.x, key.z, world.getSeed());
                }
            });
    
    public ErrorTerrainGenerator(
        IWorld iWorld,
        BiomeProvider biomeSource,
        EndGenerationSettings floatingIslandsChunkGeneratorConfig
    ) {
        super(iWorld, biomeSource, floatingIslandsChunkGeneratorConfig);
        AIR = Blocks.AIR.getDefaultState();
    }
    
    public static class TaskInfo {
        double currAverage = 0;
        boolean isInited = false;
    }
    
    
    @Override
    public void makeBase(IWorld world, IChunk chunk) {
        ChunkPrimer protoChunk = (ChunkPrimer) chunk;
        ChunkPos pos = chunk.getPos();
        Heightmap oceanFloorHeightMap = protoChunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap surfaceHeightMap = protoChunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        
        int regionX = Math.floorDiv(pos.x, regionChunkNum);
        int regionZ = Math.floorDiv(pos.z, regionChunkNum);
        RegionErrorTerrainGenerator generator = Helper.noError(() ->
            cache.get(new ChunkPos(regionX, regionZ))
        );
        
        TaskInfo taskInfo = new TaskInfo();
        
        for (int sectionY = 0; sectionY < 16; sectionY++) {
            ChunkSection section = protoChunk.func_217332_a(sectionY);
            section.lock();
            
            for (int localX = 0; localX < 16; localX++) {
                for (int localY = 0; localY < 16; localY++) {
                    for (int localZ = 0; localZ < 16; localZ++) {
                        int worldX = pos.x * 16 + localX;
                        int worldY = sectionY * 16 + localY;
                        int worldZ = pos.z * 16 + localZ;
                        
                        BlockComposition composition = generator.getBlockComposition(
                            taskInfo,
                            worldX,
                            worldY,
                            worldZ
                        );
                        
                        BlockState currBlockState;
                        
                        switch (composition) {
                            case air:
                                currBlockState = AIR;
                                break;
                            case stone:
                                currBlockState = defaultBlock;
                                break;
                            default:
                            case water:
                                currBlockState = defaultFluid;
                                break;
                        }
                        
                        if (currBlockState != AIR) {
                            section.setBlockState(localX, localY, localZ, currBlockState, false);
                            oceanFloorHeightMap.update(localX, worldY, localZ, currBlockState);
                            surfaceHeightMap.update(localX, worldY, localZ, currBlockState);
                        }
                    }
                }
            }
            
            section.unlock();
        }
        
        
    }
    
    
    enum BlockComposition {
        stone,
        water,
        air
    }
    
    
}