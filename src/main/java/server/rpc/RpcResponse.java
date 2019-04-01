package server.rpc;

import java.util.concurrent.CountDownLatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @GitHub : https://github.com/zacscoding
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RpcResponse {

    private transient CountDownLatch countDownLatch = new CountDownLatch(1);
    private String requestId;
    private String responseBody;
}
