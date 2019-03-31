package server;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication(exclude = {
    // display auto configs about messages
    org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration.class,
    org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class,
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
public class RpcQueueApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(RpcQueueApplication.class, args);
        displayBeans(ctx);
    }

    private static void displayBeans(ConfigurableApplicationContext ctx) {
        String[] beanNames = ctx.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            if (isDisplayBean(beanName)) {
                logger.info("Bean : {}", beanName);
            }
        }
    }

    private static boolean isDisplayBean(String beanName) {
        String lowerCase = beanName.toLowerCase();
        // amqp
        return lowerCase.contains("activemq") ||
            lowerCase.contains("amqp") ||
            lowerCase.contains("kafka") ||
            lowerCase.startsWith("server");
    }
}
