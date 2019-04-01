package server.rpc;

import java.util.concurrent.CompletableFuture;

/**
 * This interface is implemented depends on message queue
 *
 * @GitHub : https://github.com/zacscoding
 */
public interface RpcClient {

    RpcResponse call(RpcRequest request);

    CompletableFuture<RpcResponse> callAsync(RpcRequest rpcRequest);
}
