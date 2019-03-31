package server.configuration;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import server.configuration.condition.RpcTypeEnabledCondition.KafkaEnabledCondition;

/**
 * Kafka config
 *
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnBean(value = {RpcConfiguration.class})
@Conditional(value = KafkaEnabledCondition.class)
@Import({KafkaAutoConfiguration.class})
public class KafkaConfiguration {

    @PostConstruct
    private void setUp() {
        logger.info("Enable kafka configuration");
    }
}
