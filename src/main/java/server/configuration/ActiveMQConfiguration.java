package server.configuration;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import server.configuration.condition.RpcTypeEnabledCondition.ActiveMQEnabledCondition;

/**
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnBean(value = {RpcConfiguration.class})
@Conditional(value = ActiveMQEnabledCondition.class)
@Import({ActiveMQAutoConfiguration.class})
public class ActiveMQConfiguration {

    @PostConstruct
    private void setUp() {
        logger.info("Enable activemq configuration");
    }
}
