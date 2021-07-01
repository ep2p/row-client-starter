package io.ep2p.row.client;

import org.glassfish.tyrus.client.SslEngineConfigurator;

public interface SSLEngineConfiguratorHolder {
    SslEngineConfigurator sslEngineConfigurator();
}
