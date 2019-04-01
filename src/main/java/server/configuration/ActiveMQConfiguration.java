package server.configuration;

import javax.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import server.configuration.condition.RpcTypeEnabledCondition.ActiveMQEnabledCondition;
import server.rpc.RpcClient;
import server.rpc.execute.RpcExecutor;
import server.rpc.queue.activemq.ActiveMqRpcClient;
import server.rpc.queue.activemq.ActiveMqRpcServer;

/**
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnBean(value = {RpcConfiguration.class})
@Conditional(value = ActiveMQEnabledCondition.class)
@Import({ActiveMQAutoConfiguration.class})
public class ActiveMQConfiguration {

    /**
     * Queue for rpc request
     */
    @Bean
    public ActiveMQQueue requestDestination(
        @Value("${rpc.activemq.queue.request:requestQueue}") String requestDestinationName) {

        return new ActiveMQQueue(requestDestinationName);
    }

    /**
     * Queue for rpc reply
     */
    @Bean
    public ActiveMQQueue replyDestination(
        @Value("${rpc.activemq.queue.response:replyQueue}") String replyDestinationName) {
        return new ActiveMQQueue(replyDestinationName);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();

        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        return converter;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, @Value("${rpc.timeout:5000}") long rpcTimeout) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setReceiveTimeout(rpcTimeout);
        return jmsTemplate;
    }

    @Bean
    public ActiveMqRpcServer activeMqRpcServer(@Qualifier("rpcExecutors") TaskExecutor taskExecutor,
        JmsTemplate jmsTemplate, RpcExecutor rpcExecutor) {

        return new ActiveMqRpcServer(jmsTemplate, taskExecutor, rpcExecutor, jacksonMessageConverter());
    }

    @Bean
    public RpcClient rpcClient(JmsTemplate jmsTemplate,
        @Qualifier("requestDestination") ActiveMQQueue requestDestination,
        @Qualifier("replyDestination") ActiveMQQueue replyDestination, @Value("${rpc.timeout:5000}") long rpcTimeout) {

        return new ActiveMqRpcClient(
            jmsTemplate, jacksonMessageConverter(), requestDestination, replyDestination, rpcTimeout
        );
    }

    /**
     * Request queue listener container
     */
    @Bean
    public DefaultMessageListenerContainer jmsContainerRequest(ConnectionFactory connectionFactory,
        ActiveMqRpcServer rpcServer,
        @Qualifier("requestDestination") ActiveMQQueue requestDestination,
        @Value("${rpc.activemq.request.consumer:5}") int consumers) {

        DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();

        listenerContainer.setConnectionFactory(connectionFactory);
        listenerContainer.setDestination(requestDestination);
        listenerContainer.setMessageListener(rpcServer);
        listenerContainer.setConcurrentConsumers(consumers);

        return listenerContainer;
    }

    /**
     * Response(reply) queue listener container
     */
    @Bean
    public DefaultMessageListenerContainer jmsContainerReply(ConnectionFactory connectionFactory,
        @Qualifier("replyDestination") ActiveMQQueue replyDestination,
        RpcClient rpcClient, @Value("${rpc.activemq.response.consumer:5}") int consumers) {

        DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();

        listenerContainer.setConnectionFactory(connectionFactory);
        listenerContainer.setDestination(replyDestination);
        listenerContainer.setMessageListener(rpcClient);
        listenerContainer.setConcurrentConsumers(consumers);

        return listenerContainer;
    }
}
