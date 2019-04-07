package server.rpc.queue.rabbitmq;

import java.util.Objects;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.core.task.TaskExecutor;
import server.rpc.RpcRequest;
import server.rpc.execute.RpcExecuteResult;
import server.rpc.execute.RpcExecutor;

/**
 * Rpc Server at rabbitmq
 *
 * @GitHub : https://github.com/zacscoding
 */
public class RabbitMqRpcServer implements MessageListener {

    private RabbitTemplate rabbitTemplate;
    private RpcExecutor rpcExecutor;
    private MessageConverter messageConverter;
    private TaskExecutor taskExecutor;

    public RabbitMqRpcServer(RabbitTemplate rabbitTemplate, RpcExecutor rpcExecutor,
        MessageConverter messageConverter, TaskExecutor taskExecutor) {

        Objects.requireNonNull(rabbitTemplate, "rabbitTemplate must be not null");
        Objects.requireNonNull(rpcExecutor, "rpcExecutor must be not null");
        Objects.requireNonNull(messageConverter, "messageConverter must be not null");
        Objects.requireNonNull(taskExecutor, "taskExecutor must be not null");

        this.rabbitTemplate = rabbitTemplate;
        this.rpcExecutor = rpcExecutor;
        this.messageConverter = messageConverter;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            return;
        }

        taskExecutor.execute(() -> {
            // extract request message
            final RpcRequest request = (RpcRequest) messageConverter.fromMessage(message);

            // execute
            RpcExecuteResult executeResult = rpcExecutor.doExecute(request);

            // produce rpc response
            MessageProperties properties = message.getMessageProperties();
            rabbitTemplate.convertAndSend(
                properties.getReplyTo(), executeResult,
                new CorrelationData(message.getMessageProperties().getCorrelationId())
            );
        });
    }
}
