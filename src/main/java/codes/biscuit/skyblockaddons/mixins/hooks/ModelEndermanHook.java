package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.LocationUtils;

public class ModelEndermanHook {

    public static void setEndermanColor() {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()
                && LocationUtils.isOnZealotSpawnLocation()
                && Feature.CHANGE_ZEALOT_COLOR.isEnabled()) {
            int color = Feature.CHANGE_ZEALOT_COLOR.getColor();
            ColorUtils.bindColor(color);
        }
    }
}
