package lab.idioglossia.row.client.ws;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.CloseReason;

public class SpringRowWebsocketHandlerAdapter extends TextWebSocketHandler {
    private final SpringRowWebsocketSession springRowWebsocketSession;
    private final MessageHandler<SpringRowWebsocketSession> messageHandler;

    public SpringRowWebsocketHandlerAdapter(SpringRowWebsocketSession springRowWebsocketSession, MessageHandler<SpringRowWebsocketSession> messageHandler) {
        this.springRowWebsocketSession = springRowWebsocketSession;
        this.messageHandler = messageHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        springRowWebsocketSession.setNativeSession(session);
        messageHandler.onOpen(springRowWebsocketSession);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        messageHandler.onMessage(springRowWebsocketSession, message.getPayload());
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        messageHandler.onPong(springRowWebsocketSession, message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        messageHandler.onError(springRowWebsocketSession, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        messageHandler.onClose(springRowWebsocketSession, new CloseReason(CloseReason.CloseCodes.getCloseCode(status.getCode()), status.getReason()));
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }
}
