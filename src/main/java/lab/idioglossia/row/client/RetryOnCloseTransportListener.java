package lab.idioglossia.row.client;

import lab.idioglossia.row.client.callback.RowTransportListener;
import lab.idioglossia.row.client.ws.RowWebsocketSession;
import lab.idioglossia.row.client.ws.WebsocketSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import javax.websocket.CloseReason;

@Slf4j
public class RetryOnCloseTransportListener<S extends WebsocketSession> implements RowTransportListener<S> {
    private final RetryTemplate retryTemplate;

    public RetryOnCloseTransportListener(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    @Override
    public void onOpen(S rowWebsocketSession) {
        log.info("Stablished connection to "+ rowWebsocketSession.getUri());
    }

    @Override
    public void onError(S rowWebsocketSession, Throwable throwable) {
        log.error("Transport error", throwable);
    }

    @SneakyThrows
    @Override
    public final void onClose(RowClient rowClient, S rowWebsocketSession, CloseReason closeReason) {
        retryTemplate.execute(new RetryCallback<Void, Throwable>() {
            @Override
            public Void doWithRetry(RetryContext retryContext) throws Throwable {
                log.warn("Re-opening row client. Closed due to: "+ closeReason.getReasonPhrase());
                rowClient.open();
                return null;
            }
        });
    }
}
