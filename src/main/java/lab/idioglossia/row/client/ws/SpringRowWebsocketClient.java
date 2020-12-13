package lab.idioglossia.row.client.ws;

import lab.idioglossia.row.client.RowClient;
import lab.idioglossia.row.client.callback.ResponseCallback;
import lab.idioglossia.row.client.callback.SubscriptionListener;
import lab.idioglossia.row.client.model.RowRequest;
import lab.idioglossia.row.client.tyrus.RequestSender;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.tyrus.RowMessageHandler;
import lab.idioglossia.row.client.ws.handler.PipelineFactory;
import lombok.SneakyThrows;
import org.glassfish.tyrus.client.ClientManager;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static lab.idioglossia.row.client.model.protocol.Naming.ROW_PROTOCOL_NAME;

public class SpringRowWebsocketClient implements RowClient {
    private RequestSender requestSender;
    private SpringRowWebsocketSession springRowWebsocketSession;
    private final RowClientConfig<SpringRowWebsocketSession> rowClientConfig;

    public SpringRowWebsocketClient(RowClientConfig<SpringRowWebsocketSession> rowClientConfig) {
        this.rowClientConfig = rowClientConfig;
        this.requestSender = new RequestSender(rowClientConfig.getConnectionRepository(), rowClientConfig.getMessageIdGenerator(), rowClientConfig.getCallbackRegistry(), rowClientConfig.getSubscriptionListenerRegistry(), rowClientConfig.getMessageConverter());
    }


    @Override
    public void sendRequest(RowRequest<?, ?> rowRequest, ResponseCallback<?> responseCallback) throws IOException {
        requestSender.sendRequest(rowRequest, responseCallback);
    }

    @Override
    public void subscribe(RowRequest<?, ?> rowRequest, ResponseCallback<?> responseCallback, SubscriptionListener<?> subscriptionListener) throws IOException {
        requestSender.sendSubscribe(rowRequest, responseCallback, subscriptionListener);
    }

    @SneakyThrows
    @Override
    public void open() {
        URI uri = URI.create(rowClientConfig.getAddress());
        RowMessageHandler<SpringRowWebsocketSession> rowMessageHandler = rowClientConfig.getRowMessageHandlerProvider().provide(this.rowClientConfig, this);
        this.springRowWebsocketSession = new SpringRowWebsocketSession(rowClientConfig.getAttributes(), uri, rowClientConfig.getWebsocketConfig());
        SpringRowWebsocketHandlerAdapter springRowWebsocketHandlerAdapter = new SpringRowWebsocketHandlerAdapter(this.springRowWebsocketSession,rowMessageHandler);
        Callable<Void> callableClient = getCallableClient(springRowWebsocketHandlerAdapter, uri, rowClientConfig.getHandshakeHeadersProvider().getHeaders());
        if(this.rowClientConfig.getExecutorService() != null) {
            this.rowClientConfig.getExecutorService().submit(callableClient);
        }else {
            callableClient.call();
        }
    }

    private Callable<Void> getCallableClient(SpringRowWebsocketHandlerAdapter springRowWebsocketHandlerAdapter, URI uri, Map<String, List<String>> headers){
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                StandardWebSocketClient webSocketClient = new StandardWebSocketClient(getWebSocketContainer(rowClientConfig.getWebsocketConfig()));

                webSocketClient.doHandshake(springRowWebsocketHandlerAdapter, SpringWebSocketHttpHeaders.from(headers), uri).get();
                return null;
            }
        };
    }

    private static class SpringWebSocketHttpHeaders extends WebSocketHttpHeaders {
        public static SpringWebSocketHttpHeaders from(Map<String, List<String>> headers){
            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::addAll);
            return new SpringWebSocketHttpHeaders(httpHeaders);
        }

        public SpringWebSocketHttpHeaders(HttpHeaders httpHeaders) {
            super(httpHeaders);
            setSecWebSocketExtensions(Collections.<WebSocketExtension>emptyList());
            setSecWebSocketProtocol(Collections.singletonList(ROW_PROTOCOL_NAME));
        }
    }

    private WebSocketContainer getWebSocketContainer(WebsocketConfig websocketConfig) {
        ClientManager clientManager = new ClientManager();
        clientManager.setAsyncSendTimeout(websocketConfig.getAsyncSendTimeout());
        clientManager.setDefaultMaxSessionIdleTimeout(websocketConfig.getMaxSessionIdleTimeout());
        clientManager.setDefaultMaxBinaryMessageBufferSize(websocketConfig.getMaxBinaryMessageBufferSize());
        clientManager.setDefaultMaxTextMessageBufferSize(websocketConfig.getMaxBinaryMessageBufferSize());
        if (websocketConfig.getSslEngineConfigurator() != null) {
            clientManager.getProperties().put("SSL_CONTEXT_PROPERTY", websocketConfig.getSslEngineConfigurator().getSslContext());
            clientManager.getProperties().put("SSL_PROTOCOLS_PROPERTY", websocketConfig.getSslEngineConfigurator().getEnabledProtocols());
        }

        return clientManager;
    }

    @SneakyThrows
    @Override
    public synchronized void close() {
        if(springRowWebsocketSession != null){
            springRowWebsocketSession.close();
        }
    }
}
