package grpc.client;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import proto.TestServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GrpcClient {

    private final String CLIENT_IP;

    private final int CLIENT_PORT;

    private final Channel channel;

    public GrpcClient(String clientHost, int clientPort) {
        this.CLIENT_IP = clientHost;
        this.CLIENT_PORT = clientPort;
        this.channel = ManagedChannelBuilder.forAddress(CLIENT_IP, CLIENT_PORT).usePlaintext().build();
    }

    public <Result> Result run(Functional<TestServiceGrpc.TestServiceBlockingStub, Result> functional) {

        TestServiceGrpc.TestServiceBlockingStub testServiceBlockingStub = TestServiceGrpc.newBlockingStub(channel);

        Result result = functional.run(testServiceBlockingStub);

        return result;
    }

}