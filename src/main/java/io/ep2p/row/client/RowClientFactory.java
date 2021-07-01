package io.ep2p.row.client;

import io.ep2p.row.client.ws.WebsocketSession;

public interface RowClientFactory<S extends WebsocketSession> {
    RowClient getRowClient(String address);
    RowClient getRowClient(RowClientConfig<S> rowClientConfig);
    RowClient getRowClient(RowClientConfig<S> rowClientConfig, RowHttpClient rowHttpClient);
    RowClientConfig<S> getRowClientConfig();
}
