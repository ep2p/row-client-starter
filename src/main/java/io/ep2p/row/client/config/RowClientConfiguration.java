package io.ep2p.row.client.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.ep2p.row.client.*;
import io.ep2p.row.client.callback.GeneralCallback;
import io.ep2p.row.client.callback.RowTransportListener;
import io.ep2p.row.client.config.properties.RowClientProperties;
import io.ep2p.row.client.config.properties.WebSocketProperties;
import io.ep2p.row.client.http.RowHttpClientHolder;
import io.ep2p.row.client.registry.CallbackRegistry;
import io.ep2p.row.client.registry.MapCallbackRegistry;
import io.ep2p.row.client.registry.MapSubscriptionListenerRegistry;
import io.ep2p.row.client.registry.SubscriptionListenerRegistry;
import io.ep2p.row.client.util.DefaultJacksonMessageConverter;
import io.ep2p.row.client.util.MessageConverter;
import io.ep2p.row.client.ws.HandshakeHeadersProvider;
import io.ep2p.row.client.ws.WebsocketConfig;
import io.ep2p.row.client.ws.WebsocketSession;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

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

    @Bean("objectMapper")
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Bean("messageConverter")
    @ConditionalOnMissingBean(MessageConverter.class)
    @DependsOn("objectMapper")
    public MessageConverter messageConverter(ObjectMapper objectMapper){
        return new DefaultJacksonMessageConverter(objectMapper);
    }

    @Bean("rowRetryTemplate")
    public RetryTemplate rowRetryTemplate(){
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(5000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean("rowTransportListener")
    @ConditionalOnMissingBean(RowTransportListener.class)
    public RowTransportListener rowTransportListener(RetryTemplate rowRetryTemplate){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
        threadPoolTaskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(false);
        threadPoolTaskScheduler.initialize();
        return new HeartbeatTransportListenerDecorator(new RetryOnCloseTransportListener(rowRetryTemplate), threadPoolTaskScheduler, 5);
    }

    @Bean("sslEngineConfiguratorHolder")
    @ConditionalOnMissingBean(SSLEngineConfiguratorHolder.class)
    public SSLEngineConfiguratorHolder sslEngineConfiguratorHolder(){
        return new SSLEngineConfiguratorHolder() {
            @Override
            public SslEngineConfigurator sslEngineConfigurator() {
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
    public GeneralCallback generalCallback(){
        return new DefaultGeneralCallback();
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
    public ConnectionRepository<WebsocketSession> rowConnectionRepository(){
        return new ConnectionRepository.DefaultConnectionRepository<>();
    }

    @Bean("rowCallbackRegistry")
    @ConditionalOnMissingBean(CallbackRegistry.class)
    public CallbackRegistry rowCallbackRegistry(){
        return new MapCallbackRegistry();
    }

    @Bean("rowMessageHandlerProvider")
    @ConditionalOnMissingBean(RowMessageHandlerProvider.class)
    public RowMessageHandlerProvider rowMessageHandlerProvider(){
        return new RowMessageHandlerProvider.Default();
    }

    @Bean({"rowClientConfig", "defaultRowClientConfig"})
    @ConditionalOnMissingBean(RowClientConfig.class)
    @DependsOn({"websocketConfig", "subscriptionListenerRegistry", "messageIdGenerator", "rowTransportListener", "handshakeHeadersProvider", "generalCallback", "rowClientExecutorServiceHolder", "rowConnectionRepository", "rowCallbackRegistry", "messageConverter", "rowMessageHandlerProvider"})
    public RowClientConfig rowClientConfig(
            WebsocketConfig websocketConfig,
            SubscriptionListenerRegistry subscriptionListenerRegistry,
            MessageIdGenerator messageIdGenerator,
            RowTransportListener<WebsocketSession> rowTransportListener,
            HandshakeHeadersProvider handshakeHeadersProvider,
            GeneralCallback<?> generalCallback,
            RowClientExecutorServiceHolder rowClientExecutorServiceHolder,
            ConnectionRepository<WebsocketSession> rowConnectionRepository,
            CallbackRegistry rowCallbackRegistry,
            MessageConverter messageConverter,
            RowMessageHandlerProvider rowMessageHandlerProvider
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
                .messageConverter(messageConverter)
                .rowMessageHandlerProvider(rowMessageHandlerProvider)
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
    public RowClientFactory rowClientFactory(RowClientConfig<WebsocketSession> rowClientConfig, RowHttpClientHolder rowHttpClientHolder){
        if(rowClientProperties.getType().equals(RowClientProperties.Type.TYRUS)){
            return new TyrusRowClientFactory(rowClientConfig, rowHttpClientHolder.getRowHttpClient());
        }else {
            return new SpringRowClientFactory(rowClientConfig, rowHttpClientHolder.getRowHttpClient());
        }
    }

    @ConditionalOnProperty(prefix = "row.client", name = "address")
    @ConditionalOnMissingBean(RowClient.class)
    @Bean("rowClient")
    @DependsOn({"rowClientFactory"})
    public RowClient rowClient(RowClientFactory rowClientFactory){
        RowClient rowClient = rowClientFactory.getRowClient(rowClientProperties.getAddress());
        rowClient.open();
        return rowClient;
    }

}
