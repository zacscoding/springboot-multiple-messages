package server.rpc.execute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import server.rpc.RpcRequest;
import server.rpc.RpcResponse;

/**
 * Parse request payload to upper case
 *
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j
@Component
public class UppercaseRpcExecutor implements RpcExecutor {

    @Override
    public RpcExecuteResult doExecute(RpcRequest rpcRequest) {
        logger.info("[RPC-EXECUTOR] execute rpc request :{}", rpcRequest);

        try {
            if (rpcRequest.getSleep() > 0L) {
                Thread.sleep(rpcRequest.getSleep());
            }
        } catch (Exception e) {
        }

        return RpcExecuteResult.builder()
            .response(rpcRequest.getPayload().toUpperCase())
            .build();
    }
}
