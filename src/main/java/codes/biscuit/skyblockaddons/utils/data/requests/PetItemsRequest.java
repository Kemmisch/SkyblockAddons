package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.PetManager;
import codes.biscuit.skyblockaddons.utils.data.DataFetchCallback;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.PetItem;

import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

public class PetItemsRequest extends RemoteFileRequest<HashMap<String, PetItem>> {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public PetItemsRequest() {
        super(
                "skyblock/petItems.json",
                new JSONResponseHandler<>(new TypeToken<HashMap<String, PetItem>>() {}.getType()),
                new PetItemsCallback(getCDNBaseURL() + "skyblock/petItems.json")
        );
    }

    public static class PetItemsCallback extends DataFetchCallback<HashMap<String, PetItem>> {

        public PetItemsCallback(String path) {
            super(LOGGER, URI.create(path));
        }

        @Override
        public void completed(HashMap<String, PetItem> result) {
            super.completed(result);
            PetManager.setPetItems(Objects.requireNonNull(result, NO_DATA_RECEIVED_ERROR));
        }
    }
}
