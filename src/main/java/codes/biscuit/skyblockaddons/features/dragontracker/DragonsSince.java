package codes.biscuit.skyblockaddons.features.dragontracker;

import codes.biscuit.skyblockaddons.core.SkyblockRarity;
import codes.biscuit.skyblockaddons.core.Translations;
import com.google.common.base.CaseFormat;
import lombok.Getter;

@Getter
public enum DragonsSince {
    SUPERIOR(SkyblockRarity.LEGENDARY),
    ASPECT_OF_THE_DRAGONS(SkyblockRarity.LEGENDARY),
    ENDER_DRAGON_PET(SkyblockRarity.LEGENDARY);

    private final SkyblockRarity itemRarity;

    DragonsSince(SkyblockRarity itemRarity) {
        this.itemRarity = itemRarity;
    }

    public String getDisplayName() {
        return Translations.getMessage("dragonTracker." +  CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name()));
    }
}
