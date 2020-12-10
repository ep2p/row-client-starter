package lab.idioglossia.row.client.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "row.client.ws")
@Getter
@Setter
public class WebSocketProperties {
    private int maxBinaryBuffer = 8 * 1024 * 1024;
    private int maxTextBuffer = 4 * 1024 * 1024;
    private long maximumSessionIdle = 5 * 60 * 1000;
    private long maximumAsyncSendTimeout = 60000;
}
