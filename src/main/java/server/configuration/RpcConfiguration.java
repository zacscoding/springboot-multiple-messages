package server.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Rpc configuration for enable or disable
 *
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j(topic = "CONFIG")
@Configuration
@ConditionalOnProperty(name = "rpc.enabled", havingValue = "true")
public class RpcConfiguration {

    /**
     * TaskExecutor core pool size
     */
    @Value("${rpc.executors.core:-1}")
    private int corePoolSize;
    /**
     * TaskExecutor max pool size
     */
    @Value("${rpc.executors.max.poolsize:200}")
    private int maxPoolSize;

    /**
     * @return Rpc 실행 TaskExecutor bean(rpc request consumer)
     */
    @Bean
    public TaskExecutor rpcExecutors() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        if (corePoolSize < 1) {
            corePoolSize = getAvailableProcessors();
        }

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("rpc-server");
        executor.initialize();
        executor.setDaemon(true);

        return executor;
    }

    private int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }
}
