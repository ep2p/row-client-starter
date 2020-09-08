package lab.idioglossia.row.config;

import lab.idioglossia.row.client.*;
import lab.idioglossia.row.client.callback.GeneralCallback;
import lab.idioglossia.row.client.callback.RowTransportListener;
import lab.idioglossia.row.client.registry.CallbackRegistry;
import lab.idioglossia.row.client.registry.MapCallbackRegistry;
import lab.idioglossia.row.client.registry.MapSubscriptionListenerRegistry;
import lab.idioglossia.row.client.registry.SubscriptionListenerRegistry;
import lab.idioglossia.row.client.tyrus.ConnectionRepository;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.tyrus.UUIDMessageIdGenerator;
import lab.idioglossia.row.client.ws.HandshakeHeadersProvider;
import lab.idioglossia.row.client.ws.RowWebsocketSession;
import lab.idioglossia.row.client.ws.WebsocketConfig;
import lab.idioglossia.row.config.properties.RowClientProperties;
import lab.idioglossia.row.config.properties.WebSocketProperties;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.concurrent.ExecutorService;

@Configuration
@EnableConfigurationProperties({WebSocketProperties.class, RowClientProperties.class})
@ConditionalOnProperty(value = "row.client.enable", havingValue = "true")
@Slf4j
public class RowClientConfiguration {
    private final WebSocketProperties webSocketProperties;
    private final RowClientProperties rowClientProperties;

    public RowClientConfiguration(WebSocketProperties webSocketProperties, RowClientProperties rowClientProperties) {
        this.webSocketProperties = webSocketProperties;
        this.rowClientProperties = rowClientProperties;
    }

    @Bean("rowRetryTemplate")
    public RetryTemplate rowRetryTemplate(){
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(5000l);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean("rowTransportListener")
    @ConditionalOnMissingBean(RowTransportListener.class)
    public RowTransportListener rowTransportListener(RetryTemplate rowRetryTemplate){
        return new RetryOnCloseTransportListener(rowRetryTemplate);
    }

    @Bean("sslEngineConfiguratorHolder")
    @ConditionalOnMissingBean(SSLEngineConfiguratorHolder.class)
    public SSLEngineConfiguratorHolder sslEngineConfiguratorHolder(){
        return new SSLEngineConfiguratorHolder() {
            @Override
            public SSLEngineConfigurator sslEngineConfigurator() {
                return null;
            }
        };
    }

    @Bean("websocketConfig")
    @ConditionalOnMissingBean(WebsocketConfig.class)
    @DependsOn("sslEngineConfiguratorHolder")
    public WebsocketConfig websocketConfig(SSLEngineConfiguratorHolder sslEngineConfiguratorHolder){
        return WebsocketConfig.builder()
                .asyncSendTimeout(webSocketProperties.getMaximumAsyncSendTimeout())
                .maxBinaryMessageBufferSize(webSocketProperties.getMaxBinaryBuffer())
                .maxSessionIdleTimeout(webSocketProperties.getMaximumSessionIdle())
                .maxTextMessageBufferSize(webSocketProperties.getMaxTextBuffer())
                .sslEngineConfigurator(sslEngineConfiguratorHolder.sslEngineConfigurator())
                .build();
    }

    @Bean("subscriptionListenerRegistry")
    @ConditionalOnMissingBean(SubscriptionListenerRegistry.class)
    public SubscriptionListenerRegistry subscriptionListenerRegistry(){
        return new MapSubscriptionListenerRegistry();
    }

    @Bean("messageIdGenerator")
    @ConditionalOnMissingBean(MessageIdGenerator.class)
    public MessageIdGenerator messageIdGenerator(){
        return new UUIDMessageIdGenerator();
    }

    @Bean("handshakeHeadersProvider")
    @ConditionalOnMissingBean(HandshakeHeadersProvider.class)
    public HandshakeHeadersProvider handshakeHeadersProvider(){
        return new HandshakeHeadersProvider.Default();
    }

    @Bean
    @ConditionalOnMissingBean(GeneralCallback.class)
    public GeneralCallback<?> generalCallback(){
        return new DefaultGeneralCallback<>();
    }

    @Bean("rowClientExecutorServiceHolder")
    @ConditionalOnMissingBean(RowClientExecutorServiceHolder.class)
    public RowClientExecutorServiceHolder rowClientExecutorServiceHolder(){
        return new RowClientExecutorServiceHolder() {
            @Override
            public ExecutorService getExecutorService() {
                return null;
            }
        };
    }

    @Bean("rowConnectionRepository")
    @ConditionalOnMissingBean(ConnectionRepository.class)
    public ConnectionRepository<RowWebsocketSession> rowConnectionRepository(){
        return new ConnectionRepository.DefaultConnectionRepository<>();
    }

    @Bean("rowCallbackRegistry")
    @ConditionalOnMissingBean(CallbackRegistry.class)
    public CallbackRegistry rowCallbackRegistry(){
        return new MapCallbackRegistry();
    }

    @Bean({"rowClientConfig", "defaultRowClientConfig"})
    @ConditionalOnMissingBean(RowClientConfig.class)
    @DependsOn({"websocketConfig", "subscriptionListenerRegistry", "messageIdGenerator", "rowTransportListener", "handshakeHeadersProvider", "generalCallback", "rowClientExecutorServiceHolder", "rowConnectionRepository", "rowCallbackRegistry"})
    public RowClientConfig rowClientConfig(
            WebsocketConfig websocketConfig,
            SubscriptionListenerRegistry subscriptionListenerRegistry,
            MessageIdGenerator messageIdGenerator,
            RowTransportListener rowTransportListener,
            HandshakeHeadersProvider handshakeHeadersProvider,
            GeneralCallback<?> generalCallback,
            RowClientExecutorServiceHolder rowClientExecutorServiceHolder,
            ConnectionRepository<RowWebsocketSession> rowConnectionRepository,
            CallbackRegistry rowCallbackRegistry
    ){
        return RowClientConfig.builder()
                .websocketConfig(websocketConfig)
                .subscriptionListenerRegistry(subscriptionListenerRegistry)
                .messageIdGenerator(messageIdGenerator)
                .rowTransportListener(rowTransportListener)
                .handshakeHeadersProvider(handshakeHeadersProvider)
                .generalCallback(generalCallback)
                .executorService(rowClientExecutorServiceHolder.getExecutorService())
                .connectionRepository(rowConnectionRepository)
                .callbackRegistry(rowCallbackRegistry)
                .build();
    }

    @Bean("rowHttpClientHolder")
    @ConditionalOnMissingBean(RowHttpClientHolder.class)
    public RowHttpClientHolder rowHttpClientHolder(){
        return new RowHttpClientHolder() {
            @Override
            public RowHttpClient getRowHttpClient() {
                return null;
            }
        };
    }

    @Bean("rowClientFactory")
    @DependsOn({"rowClientConfig", "rowHttpClientHolder"})
    public RowClientFactory rowClientFactory(RowClientConfig rowClientConfig, RowHttpClientHolder rowHttpClientHolder){
        return new RowClientFactory(rowClientConfig, rowHttpClientHolder.getRowHttpClient());
    }

    @ConditionalOnProperty(prefix = "row.client", name = "address", matchIfMissing = true)
    @ConditionalOnMissingBean(RowClient.class)
    @Bean("rowClient")
    @DependsOn({"rowClientFactory"})
    public RowClient rowClient(RowClientFactory rowClientFactory){
        RowClient rowClient = rowClientFactory.getRowClient(rowClientProperties.getAddress());
        rowClient.open();
        return rowClient;
    }

}
