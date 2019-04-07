package server.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import server.configuration.condition.RpcTypeEnabledCondition.RabbitMQEnabledCondition;
import server.rpc.RpcClient;
import server.rpc.execute.RpcExecutor;
import server.rpc.queue.rabbitmq.RabbitMqRpcClient;
import server.rpc.queue.rabbitmq.RabbitMqRpcServer;

/**
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnBean(value = {RpcConfiguration.class})
@Conditional(value = RabbitMQEnabledCondition.class)
@AutoConfigureAfter({RabbitAutoConfiguration.class})
public class RabbitMQConfiguration {

    private String host;
    private String user;
    private String password;
    private String exchangeValue;
    private String requestDestination;
    private String routingKey;
    private Long rpcTimeout;
    private String listenerConcurrent;

    public RabbitMQConfiguration(
        @Value("${rpc.rabbitmq.host}") String host,
        @Value("${rpc.rabbitmq.user}") String user,
        @Value("${rpc.rabbitmq.password}") String password,
        @Value("${rpc.rabbitmq.queue.exchange}") String exchangeValue,
        @Value("${rpc.rabbitmq.queue.request}") String requestDestination,
        @Value("${rpc.rabbitmq.queue.route}") String routingKey,
        @Value("${rpc.timeout}") Long rpcTimeout,
        @Value("${rpc.rabbitmq.concurrent.request}") String listenerConcurrent) {

        this.host = host;
        this.user = user;
        this.password = password;
        this.exchangeValue = exchangeValue;
        this.requestDestination = requestDestination;
        this.routingKey = routingKey;
        this.rpcTimeout = rpcTimeout;
        this.listenerConcurrent = listenerConcurrent;
    }

    @Bean
    public DirectExchange rpcExchange() {
        return new DirectExchange(exchangeValue);
    }

    @Bean
    public Queue requestQueue() {
        return new Queue(requestDestination);
    }

    @Bean
    public Binding rpcServerBinding() {
        return BindingBuilder.bind(requestQueue()).to(rpcExchange()).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter producerMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RpcClient rpcClient() {
        return new RabbitMqRpcClient(rabbitTemplate(), rpcExchange(), routingKey);
    }

    @Bean
    public RabbitMqRpcServer rabbitMqRpcServer(RpcExecutor rpcExecutor,
        @Qualifier("rpcExecutors") TaskExecutor taskExecutor) {
        return new RabbitMqRpcServer(rabbitTemplate(), rpcExecutor, producerMessageConverter(), taskExecutor);
    }


    /**
     * RPC request queue listeners
     */
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(RabbitMqRpcServer rabbitMqRpcServer) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();

        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(requestDestination);
        container.setConcurrency(listenerConcurrent);
        container.setMessageListener(rabbitMqRpcServer);

        return container;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();

        rabbitTemplate.setConnectionFactory(connectionFactory());
        rabbitTemplate.setReceiveTimeout(rpcTimeout);
        rabbitTemplate.setMessageConverter(producerMessageConverter());

        return rabbitTemplate;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();

        factory.setHost(host);
        factory.setUsername(user);
        factory.setPassword(password);

        return factory;
    }
}