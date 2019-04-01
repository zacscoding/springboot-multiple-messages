package server.rpc.queue.activemq;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.scheduling.annotation.Async;
import server.rpc.RpcClient;
import server.rpc.RpcRequest;
import server.rpc.RpcResponse;

/**
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j
public class ActiveMqRpcClient implements RpcClient, MessageListener {

    private JmsTemplate jmsTemplate;
    private MessageConverter messageConverter;
    private Destination requestDestination;
    private Destination replyDestination;
    private ConcurrentMap<String, RpcResponse> concurrentMap;
    private long timeout;

    public ActiveMqRpcClient(JmsTemplate jmsTemplate, MessageConverter messageConverter, Destination requestDestination,
        Destination replyDestination, long timeout) {

        Objects.requireNonNull(jmsTemplate, "jmsTemplate must be not null");
        Objects.requireNonNull(messageConverter, "messageConverter must be not null");
        Objects.requireNonNull(requestDestination, "requestDestination must be not null");
        Objects.requireNonNull(replyDestination, "replyDestination must be not null");

        this.jmsTemplate = jmsTemplate;
        this.messageConverter = messageConverter;
        this.requestDestination = requestDestination;
        this.replyDestination = replyDestination;
        this.concurrentMap = new ConcurrentHashMap<>();
        this.timeout = timeout;
    }

    @Override
    public RpcResponse call(RpcRequest request) {

        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(request.getRequestId());

        final String correlationID = UUID.randomUUID().toString();
        concurrentMap.put(correlationID, rpcResponse);

        // produce RpcRequest message
        jmsTemplate.send(requestDestination, session -> {
            Message requestMessage = messageConverter.toMessage(request, session);

            requestMessage.setJMSCorrelationID(correlationID);
            requestMessage.setJMSReplyTo(replyDestination);

            return requestMessage;
        });

        logger.debug("Success to produce request message. id : {}", request.getRequestId());

        try {
            // wait for reply
            boolean isReceived = rpcResponse.getCountDownLatch().await(timeout, TimeUnit.MILLISECONDS);
            RpcResponse result = concurrentMap.remove(correlationID);

            if (isReceived && result != null) {
                logger.debug("Success to receive rpc request. result : {}", result);
                return rpcResponse;
            }
        } catch (InterruptedException e) {
            // timeout exception
            logger.warn("InterruptedException occur");
            // TODO : throw timeout exception
        }

        return null;
    }

    @Override
    public CompletableFuture<RpcResponse> callAsync(RpcRequest request) {
        throw new UnsupportedOperationException("Not yet");
    }

    /**
     * Rpc response consume
     */
    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;

            try {
                RpcResponse rpcResponse = concurrentMap.get(textMessage.getJMSCorrelationID());
                if (rpcResponse == null) {
                    logger.warn("Already removed after occur timeout exception. :: " + textMessage.getText());
                    // TODO :: send callback
                    return;
                }

                rpcResponse.setResponseBody(textMessage.getText());
                rpcResponse.getCountDownLatch().countDown();
            } catch (JMSException e) {
                logger.warn("JMSException occur while handle on message", e);
            }
        } else {
            logger.warn("Received message but not TestMessage. class type : {}", message.getClass().getName());
        }
    }
}
