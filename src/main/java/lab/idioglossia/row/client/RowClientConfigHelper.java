package lab.idioglossia.row.client;

import lab.idioglossia.row.client.tyrus.ConnectionRepository;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.ws.RowWebsocketSession;

public class RowClientConfigHelper {
    public static RowClientConfig<RowWebsocketSession> clone(RowClientConfig<RowWebsocketSession> rowClientConfig){
        return RowClientConfig.<RowWebsocketSession>builder()
                .address(rowClientConfig.getAddress())
                .callbackRegistry(rowClientConfig.getCallbackRegistry())
                .attributes(rowClientConfig.getAttributes())
                .connectionRepository(new ConnectionRepository.DefaultConnectionRepository<>())
                .executorService(rowClientConfig.getExecutorService())
                .generalCallback(new DefaultGeneralCallback())
                .rowMessageHandlerProvider(rowClientConfig.getRowMessageHandlerProvider())
                .handshakeHeadersProvider(rowClientConfig.getHandshakeHeadersProvider())
                .rowTransportListener(rowClientConfig.getRowTransportListener())
                .messageIdGenerator(rowClientConfig.getMessageIdGenerator())
                .subscriptionListenerRegistry(rowClientConfig.getSubscriptionListenerRegistry())
                .websocketConfig(rowClientConfig.getWebsocketConfig())
                .build();
    }
}
