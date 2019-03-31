package server.configuration;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Rpc configuration for enable or disable
 *
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnProperty(name = "rpc.enabled", havingValue = "true")
public class RpcConfiguration {

    @PostConstruct
    private void setUp() {
        logger.info("RpcConfiguration is enabled");
    }
}
