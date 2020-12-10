package lab.idioglossia.row.client;

import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.tyrus.TyrusRowWebsocketClient;
import lab.idioglossia.row.client.ws.WebsocketSession;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public interface RowClientFactory<S extends WebsocketSession> {
    RowClient getRowClient(String address);
    RowClient getRowClient(RowClientConfig<S> rowClientConfig);
    RowClient getRowClient(RowClientConfig<S> rowClientConfig, RowHttpClient rowHttpClient);
    RowClientConfig<S> getRowClientConfig();
}
