package lab.idioglossia.row.client;

import lab.idioglossia.row.client.callback.RowTransportListener;
import lab.idioglossia.row.client.ws.RowWebsocketSession;

import javax.websocket.CloseReason;

public abstract class TransportListenerDecorator implements RowTransportListener {
    private final RowTransportListener rowTransportListener;

    public TransportListenerDecorator(RowTransportListener rowTransportListener) {
        this.rowTransportListener = rowTransportListener;
    }

    @Override
    public void onOpen(RowWebsocketSession rowWebsocketSession) {
        this.rowTransportListener.onOpen(rowWebsocketSession);
    }

    @Override
    public void onError(RowWebsocketSession rowWebsocketSession, Throwable throwable) {
        this.rowTransportListener.onError(rowWebsocketSession, throwable);
    }

    @Override
    public void onClose(RowClient rowClient, RowWebsocketSession rowWebsocketSession, CloseReason closeReason) {
        this.rowTransportListener.onClose(rowClient, rowWebsocketSession, closeReason);
    }
}
