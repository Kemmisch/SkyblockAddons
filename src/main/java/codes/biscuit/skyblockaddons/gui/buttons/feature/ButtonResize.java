package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

@Getter
public class ButtonResize extends ButtonFeature {

    private static final int SIZE = 2;

    private final Corner corner;
    public float x;
    public float y;

    public ButtonResize(float x, float y, Feature feature, Corner corner) {
        super(0, 0, 0, "", feature);
        this.corner = corner;
        this.x = x;
        this.y = y;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float scale = feature.getGuiScale();
        hovered = mouseX >= (x - SIZE) * scale && mouseY >= (y - SIZE) * scale
                && mouseX < (x + SIZE) * scale && mouseY < (y + SIZE) * scale;
        int color = hovered ? ColorCode.WHITE.getColor() : ColorCode.WHITE.getColor(70);

        GlStateManager.enableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        DrawUtils.drawRectAbsolute(x - SIZE,y - SIZE, x + SIZE, y + SIZE, color);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return hovered;
    }

    public enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }
}
