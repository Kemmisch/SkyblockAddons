package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.features.enchants.EnchantManager;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.EnchantmentsData;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Objects;

public class EnchantmentsRequest extends RemoteFileRequest<EnchantmentsData> {
    private static final Logger LOGGER = LogManager.getLogger();

    public EnchantmentsRequest() {
        super(
                "skyblock/enchants.json",
                new JSONResponseHandler<>(new TypeToken<EnchantmentsData>() {}.getType()),
                new EnchantmentsCallback(getCDNBaseURL() + "skyblock/enchants.json")
        );
    }

    public static class EnchantmentsCallback extends DataFetchCallback<EnchantmentsData> {

        public EnchantmentsCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(EnchantmentsData result) {
            super.completed(result);
            EnchantManager.setEnchants(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }
    }
}
