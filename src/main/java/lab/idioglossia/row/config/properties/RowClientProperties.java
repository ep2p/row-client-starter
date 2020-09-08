package lab.idioglossia.row.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "row.client")
@Getter
@Setter
public class RowClientProperties {
    private String address;
    private boolean enable;
}
