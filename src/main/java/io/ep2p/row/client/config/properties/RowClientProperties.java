package io.ep2p.row.client.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "row.client")
@Getter
@Setter
public class RowClientProperties {
    private String address;
    private boolean enable;
    private Type type = Type.TYRUS;

    public enum Type {
        TYRUS, SPRING
    }
}
