package lab.idioglossia.row.client;

import lab.idioglossia.row.client.tyrus.RowClientConfig;

public class RowClientConfigHelper {
    public static RowClientConfig clone(RowClientConfig rowClientConfig){
        return RowClientConfig.builder()
                .address(rowClientConfig.getAddress())
                .callbackRegistry(rowClientConfig.getCallbackRegistry())
                .attributes(rowClientConfig.getAttributes())
                .connectionRepository(rowClientConfig.getConnectionRepository())
                .executorService(rowClientConfig.getExecutorService())
                .generalCallback(rowClientConfig.getGeneralCallback())
                .handshakeHeadersProvider(rowClientConfig.getHandshakeHeadersProvider())
                .rowTransportListener(rowClientConfig.getRowTransportListener())
                .messageIdGenerator(rowClientConfig.getMessageIdGenerator())
                .sslContext(rowClientConfig.getSslContext())
                .subscriptionListenerRegistry(rowClientConfig.getSubscriptionListenerRegistry())
                .websocketConfig(rowClientConfig.getWebsocketConfig())
                .build();
    }
}
