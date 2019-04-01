package server.rpc.execute;

import server.rpc.RpcRequest;
import server.rpc.RpcResponse;

/**
 * @GitHub : https://github.com/zacscoding
 */
public interface RpcExecutor {

    RpcExecuteResult doExecute(RpcRequest rpcRequest);
}
