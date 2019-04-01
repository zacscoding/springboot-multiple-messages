package server.rpc.queue.activemq;

import java.util.Objects;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import server.rpc.RpcRequest;
import server.rpc.execute.RpcExecuteResult;
import server.rpc.execute.RpcExecutor;

/**
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j
public class ActiveMqRpcServer implements MessageListener {

    private JmsTemplate jmsTemplate;
    private RpcExecutor rpcExecutor;
    private MessageConverter messageConverter;
    private TaskExecutor taskExecutor;

    public ActiveMqRpcServer(JmsTemplate jmsTemplate, TaskExecutor taskExecutor, RpcExecutor rpcExecutor,
        MessageConverter messageConverter) {

        Objects.requireNonNull(jmsTemplate, "jmsTemplate must be not null");
        Objects.requireNonNull(taskExecutor, "taskExecutor must be not null");
        Objects.requireNonNull(rpcExecutor, "rpcExecutor must be not null");
        Objects.requireNonNull(messageConverter, "messageConverter must be not null");

        this.jmsTemplate = jmsTemplate;
        this.rpcExecutor = rpcExecutor;
        this.taskExecutor = taskExecutor;
        this.messageConverter = messageConverter;
    }


    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            taskExecutor.execute(() -> {
                // System.out.println("## >> Check execute internal :: " + Thread.currentThread().getName() + "-" + Thread.currentThread().getId()); // TEMP
                TextMessage textMessage = (TextMessage) message;
                try {
                    final String request = textMessage.getText();
                    final RpcRequest rpcRequest = (RpcRequest) messageConverter.fromMessage(message);

                    logger.trace("consume rpc request : {}", request);
                    Destination destination = textMessage.getJMSReplyTo();

                    final String jmsCorrelationID = textMessage.getJMSCorrelationID();

                    jmsTemplate.send(destination, session -> {
                        // produce reply
                        RpcExecuteResult result = rpcExecutor.doExecute(rpcRequest);
                        // TODO :: RpcExecute에서 IOException
                        Message msg = null;

                        if (result.hasError()) {
                            logger.warn("Failed to execute rpc request. reason : " + result.getError().getMessage());
                            msg = session.createTextMessage(result.getError().getMessage());
                        } else {
                            msg = session.createTextMessage(result.getResponse());
                        }

                        msg.setJMSCorrelationID(jmsCorrelationID);
                        return msg;
                    });
                } catch (JMSException e) {
                    logger.warn("JMSException occur while handle on message", e);
                }
            });
        }
    }
}
