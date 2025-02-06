package codes.biscuit.skyblockaddons.utils.gson;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValuesManager.ConfigValues;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureData;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

/**
 * Customized deserialization of {@link ConfigValues} {@link ConfigValues} It parses each Feature in a tolerant manner.
 * If any Feature object is read incorrectly, instead of failing completely, it marks that Feature to take its default
 * value and continues reading the remaining properties.
 */
public class ConfigValuesAdapter implements JsonDeserializer<ConfigValues> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @Override
    public ConfigValues deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject;
        try {
            jsonObject = json.getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            SkyblockAddons.getInstance().getConfigValuesManager().backupConfig();
            LOGGER.error("configurations.json is corrupted! It will be restored to default settings.", e);
            return null;
        }
        ConfigValues configValues = new ConfigValues();

        if (jsonObject.has("configVersion")) {
            try {
                configValues.setConfigVersion(jsonObject.get("configVersion").getAsInt());
            } catch (Exception e) {
                LOGGER.error("Error while parsing 'configVersion':", e);
                configValues.setConfigVersion(Integer.MIN_VALUE);
            }
        }
        if (jsonObject.has("lastFeatureId")) {
            try {
                configValues.setLastFeatureId(jsonObject.get("lastFeatureId").getAsInt());
            } catch (Exception e) {
                LOGGER.error("Error while parsing 'lastFeatureId':", e);
                configValues.setLastFeatureId(Integer.MIN_VALUE);
            }
        }
        if (jsonObject.has("features")) {
            JsonObject featuresObject;
            try {
                featuresObject = jsonObject.getAsJsonObject("features");
            } catch (Exception e) {
                featuresObject = new JsonObject();
                SkyblockAddons.getInstance().getConfigValuesManager().backupConfig();
                LOGGER.error("Error while parsing 'features'! It will be restored to defaults. Error:", e);
            }
            EnumMap<Feature, FeatureData<?>> features = new EnumMap<>(Feature.class);

            for (Map.Entry<String, JsonElement> entry : featuresObject.entrySet()) {
                try {
                    features.put(Feature.valueOf(entry.getKey()), context.deserialize(entry.getValue(), FeatureData.class));
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Could not parse feature {}. If it is a deprecated feature, ignore it.", entry.getKey());
                    LOGGER.catching(e);
                } catch (JsonParseException e) {
                    LOGGER.error("Could not parse feature {}. FeatureData will be replaced with defaults.", entry.getKey());
                    LOGGER.catching(e);
                    features.put(Feature.valueOf(entry.getKey()), null);
                }
            }

            configValues.setFeatures(features);
        }

        return configValues;
    }
}