package server.configuration;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import server.configuration.condition.RpcTypeEnabledCondition.RabbitMQEnabledCondition;

/**
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnBean(value = {RpcConfiguration.class})
@Conditional(value = RabbitMQEnabledCondition.class)
@Import({RabbitAutoConfiguration.class})
public class RabbitMQConfiguration {

    @PostConstruct
    private void setUp() {
        logger.info("Enable rabbitmq configuration");
    }
}