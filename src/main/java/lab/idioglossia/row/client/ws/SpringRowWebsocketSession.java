package lab.idioglossia.row.client.ws;

import org.springframework.web.socket.*;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

public class SpringRowWebsocketSession extends AbstractWebsocketSession<WebSocketSession> {
    private final URI uri;
    private final WebsocketConfig websocketConfig;


    public SpringRowWebsocketSession(Map<String, Object> attributes, URI uri, WebsocketConfig websocketConfig) {
        super(attributes);
        this.uri = uri;
        this.websocketConfig = websocketConfig;
    }

    public void close() throws IOException {
        WebSocketSession nativeSession = getNativeSession();
        assert nativeSession != null;
        nativeSession.close();
    }

    @Override
    public URI getUri() {
        return uri;
    }

    public void close(CloseReason closeReason) throws IOException {
        WebSocketSession nativeSession = getNativeSession();
        assert nativeSession != null;
        nativeSession.close(new CloseStatus(closeReason.getCloseCode().getCode()));
    }

    public boolean isOpen(){
        WebSocketSession nativeSession = getNativeSession();
        return nativeSession != null && nativeSession.isOpen();
    }

    public boolean isSecure(){
        WebSocketSession nativeSession = getNativeSession();
        return nativeSession != null && websocketConfig.getSslEngineConfigurator() != null;
    }

    @Override
    public void sendTextMessage(String s) throws Exception {
        getNativeSession().sendMessage(new TextMessage(s));
    }

    @Override
    public void sendPingMessage(ByteBuffer byteBuffer) throws Exception {
        getNativeSession().sendMessage(new PingMessage(byteBuffer));
    }

    @Override
    public void sendPongMessage(ByteBuffer byteBuffer) throws Exception {
        getNativeSession().sendMessage(new PongMessage(byteBuffer));
    }

    @Override
    public void closeInternal(CloseReason closeReason) throws Exception {
        getNativeSession().close(new CloseStatus(closeReason.getCloseCode().getCode()));
    }
}
