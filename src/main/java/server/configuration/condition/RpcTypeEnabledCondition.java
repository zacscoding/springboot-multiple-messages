package server.configuration.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Rpc type conditions
 *
 * - ActiveMQ   : rpc.type=activemq
 * - RabbitMQ   : rpc.type=rabbitmq
 * - Kafka      : rpc.type=kafka
 *
 * @GitHub : https://github.com/zacscoding
 */
public class RpcTypeEnabledCondition {

    public static class ActiveMQEnabledCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "activemq".equals(context.getEnvironment().getProperty("rpc.type"));
        }
    }

    public static class RabbitMQEnabledCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "rabbitmq".equals(context.getEnvironment().getProperty("rpc.type"));
        }
    }

    public static class KafkaEnabledCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "kafka".equals(context.getEnvironment().getProperty("rpc.type"));
        }
    }
}
