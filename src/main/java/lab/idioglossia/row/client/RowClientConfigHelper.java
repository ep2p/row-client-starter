package lab.idioglossia.row.client;

import lab.idioglossia.row.client.tyrus.ConnectionRepository;
import lab.idioglossia.row.client.tyrus.RowClientConfig;

public class RowClientConfigHelper {
    public static RowClientConfig clone(RowClientConfig rowClientConfig){
        return RowClientConfig.builder()
                .address(rowClientConfig.getAddress())
                .callbackRegistry(rowClientConfig.getCallbackRegistry())
                .attributes(rowClientConfig.getAttributes())
                .connectionRepository(new ConnectionRepository.DefaultConnectionRepository<>())
                .executorService(rowClientConfig.getExecutorService())
                .generalCallback(new DefaultGeneralCallback<>())
                .handshakeHeadersProvider(rowClientConfig.getHandshakeHeadersProvider())
                .rowTransportListener(rowClientConfig.getRowTransportListener())
                .messageIdGenerator(rowClientConfig.getMessageIdGenerator())
                .subscriptionListenerRegistry(rowClientConfig.getSubscriptionListenerRegistry())
                .websocketConfig(rowClientConfig.getWebsocketConfig())
                .build();
    }
}
