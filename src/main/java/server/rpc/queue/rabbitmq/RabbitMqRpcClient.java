package server.rpc.queue.rabbitmq;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import server.rpc.RpcClient;
import server.rpc.RpcRequest;
import server.rpc.RpcResponse;
import server.rpc.execute.RpcExecuteResult;

/**
 * Rpc client at rabbitmq
 *
 * @GitHub : https://github.com/zacscoding
 */
public class RabbitMqRpcClient implements RpcClient {

    private RabbitTemplate rabbitTemplate;
    private DirectExchange exchange;
    private String routeKey;

    public RabbitMqRpcClient(RabbitTemplate rabbitTemplate, DirectExchange exchange, String routeKey) {
        Objects.requireNonNull(rabbitTemplate, "rabbitTemplate must be not null");
        Objects.requireNonNull(exchange, "exchange must be not null");
        Objects.requireNonNull(routeKey, "routeKey must be not null");

        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routeKey = routeKey;
    }

    @Override
    public RpcResponse call(RpcRequest request) {
        RpcResponse response = new RpcResponse(request);
        RpcExecuteResult result =
            (RpcExecuteResult) rabbitTemplate.convertSendAndReceive(exchange.getName(), routeKey, request);

        if (result == null || result.hasError()) {
            // TODO error code
            response.setResponseBody("");
        } else {
            response.setResponseBody(result.getResponse());
        }

        return response;
    }

    @Override
    public CompletableFuture<RpcResponse> callAsync(RpcRequest rpcRequest) {
        return null;
    }
}
