package server.rpc.rest;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.rpc.RpcClient;
import server.rpc.RpcRequest;
import server.rpc.RpcResponse;

/**
 * Rpc controller
 *
 * @GitHub : https://github.com/zacscoding
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/rpc")
public class RpcController {

    private final RpcClient rpcClient;

    @PostMapping
    public RpcResponse rpcRequest(@RequestBody RpcRequest rpcRequest) {
        return rpcClient.call(rpcRequest);
    }
}
