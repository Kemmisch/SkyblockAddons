package codes.biscuit.skyblockaddons.features.fishParticles;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.OverlayEffectRenderer;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class FishParticleOverlay extends OverlayEffectRenderer {

    private boolean biggerWakeCache;

    public FishParticleOverlay() {
        super();
        feature = Feature.FISHING_PARTICLE_OVERLAY;
    }

    /**
     * @return {@code true} iff the fishing particle overlay is enabled.
     */
    @Override
    public boolean shouldRenderOverlay() {
         return super.shouldRenderOverlay() && feature.isEnabled();
    }

    /**
     * Setup the fishing particle overlay render environment.
     */
    @Override
    public void setupRenderEnvironment() {
        super.setupRenderEnvironment();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        biggerWakeCache = Feature.FISHING_PARTICLE_OVERLAY.isEnabled(FeatureSetting.BIGGER_WAKE);
    }

    /**
     * End the fishing particle overlay render environment.
     */
    @Override
    public void endRenderEnvironment() {
        super.endRenderEnvironment();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
    }

    /**
     * Setup the render environment for a fish particle.
     * @param effect the fish particle effect to be rendered
     */
    @Override
    public void setupRenderEffect(EntityFX effect) {
        if (biggerWakeCache) {
            effect.particleScale *= 2;
            effect.posY += .1;
            effect.prevPosY += .1;
        }
    }

    /**
     * End the render environment for a fish particle.
     * @param effect the effect that was just rendered
     */
    @Override
    public void endRenderEffect(EntityFX effect) {
        if (biggerWakeCache) {
            effect.particleScale /= 2;
            effect.posY -= .1;
            effect.prevPosY -= .1;
        }
    }
}
