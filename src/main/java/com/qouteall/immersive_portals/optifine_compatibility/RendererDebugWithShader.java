package com.qouteall.immersive_portals.optifine_compatibility;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.qouteall.immersive_portals.CGlobal;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.render.MyGameRenderer;
import com.qouteall.immersive_portals.render.PortalRenderer;
import com.qouteall.immersive_portals.render.SecondaryFrameBuffer;
import com.qouteall.immersive_portals.render.ShaderManager;
import com.qouteall.immersive_portals.render.context_management.PortalRendering;
import com.qouteall.immersive_portals.render.context_management.RenderStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL13;

public class RendererDebugWithShader extends PortalRenderer {
    SecondaryFrameBuffer deferredBuffer = new SecondaryFrameBuffer();
    
    @Override
    public boolean shouldSkipClearing() {
        return false;
    }
    
    @Override
    public void renderPortalInEntityRenderer(Portal portal) {
        if (Shaders.isShadowPass) {
            assert false;
            //ViewAreaRenderer.drawPortalViewTriangle(portal);
        }
    }
    
    @Override
    public void onBeforeTranslucentRendering(MatrixStack matrixStack) {
    
    }
    
    @Override
    public void onAfterTranslucentRendering(MatrixStack matrixStack) {
        renderPortals(matrixStack);
    }
    
    @Override
    public void prepareRendering() {
        if (CGlobal.shaderManager == null) {
            CGlobal.shaderManager = new ShaderManager();
        }
        
        deferredBuffer.prepare();
        
        deferredBuffer.fb.setFramebufferColor(1, 0, 0, 0);
        deferredBuffer.fb.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
        
        OFGlobal.bindToShaderFrameBuffer.run();
        
    }
    
    @Override
    public void finishRendering() {
    
    }
    
    @Override
    protected void doRenderPortal(Portal portal, MatrixStack matrixStack) {
        if (RenderStates.getRenderedPortalNum() >= 1) {
            return;
        }
    
        PortalRendering.pushPortalLayer(portal);
        
        renderPortalContent(portal);
        //it will bind the gbuffer of rendered dimension
    
        PortalRendering.popPortalLayer();
        
        deferredBuffer.fb.bindFramebuffer(true);
        
        GlStateManager.activeTexture(GL13.GL_TEXTURE0);
        client.getFramebuffer().framebufferRender(
            deferredBuffer.fb.framebufferWidth,
            deferredBuffer.fb.framebufferHeight
        );
        
        OFGlobal.bindToShaderFrameBuffer.run();
    }
    
    @Override
    protected void invokeWorldRendering(
        Vec3d newEyePos, Vec3d newLastTickEyePos, ClientWorld newWorld
    ) {
        MyGameRenderer.switchAndRenderTheWorld(
            newWorld, newEyePos,
            newLastTickEyePos,
            runnable -> {
                OFGlobal.shaderContextManager.switchContextAndRun(()->{
                    OFGlobal.bindToShaderFrameBuffer.run();
                    runnable.run();
                });
            }
        );
    }
    
//    @Override
//    protected void renderPortalContentWithContextSwitched(
//        Portal portal, Vec3d oldCameraPos, ClientWorld oldWorld
//    ) {
//        OFGlobal.shaderContextManager.switchContextAndRun(
//            () -> {
//                OFGlobal.bindToShaderFrameBuffer.run();
//                super.renderPortalContentWithContextSwitched(portal, oldCameraPos, oldWorld);
//            }
//        );
//    }
    
    @Override
    public void onRenderCenterEnded(MatrixStack matrixStack) {
        if (PortalRendering.isRendering()) {
            return;
        }
        
        if (RenderStates.getRenderedPortalNum() == 0) {
            return;
        }
        
        GlStateManager.enableAlphaTest();
        Framebuffer mainFrameBuffer = client.getFramebuffer();
        mainFrameBuffer.bindFramebuffer(true);
        
        deferredBuffer.fb.framebufferRender(mainFrameBuffer.framebufferWidth, mainFrameBuffer.framebufferHeight);
    }
}
