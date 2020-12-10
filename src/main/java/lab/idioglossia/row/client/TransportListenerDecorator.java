package lab.idioglossia.row.client;

import lab.idioglossia.row.client.callback.RowTransportListener;
import lab.idioglossia.row.client.ws.RowWebsocketSession;
import lab.idioglossia.row.client.ws.WebsocketSession;

import javax.websocket.CloseReason;

public abstract class TransportListenerDecorator<S extends WebsocketSession> implements RowTransportListener<S> {
    private final RowTransportListener<S> rowTransportListener;

    public TransportListenerDecorator(RowTransportListener<S> rowTransportListener) {
        this.rowTransportListener = rowTransportListener;
    }

    @Override
    public void onOpen(S rowWebsocketSession) {
        this.rowTransportListener.onOpen(rowWebsocketSession);
    }

    @Override
    public void onError(S rowWebsocketSession, Throwable throwable) {
        this.rowTransportListener.onError(rowWebsocketSession, throwable);
    }

    @Override
    public void onClose(RowClient rowClient, S rowWebsocketSession, CloseReason closeReason) {
        this.rowTransportListener.onClose(rowClient, rowWebsocketSession, closeReason);
    }
}
