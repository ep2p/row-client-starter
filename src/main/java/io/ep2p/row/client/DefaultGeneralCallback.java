package io.ep2p.row.client;

import io.ep2p.row.client.callback.GeneralCallback;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultGeneralCallback implements GeneralCallback<String> {
    @Override
    public Class<String> getClassOfCallback() {
        return String.class;
    }

    @Override
    public void onMessage(String message) {
        log.warn("Implement general callback to get messages published from server into no specific channel");
    }
}
