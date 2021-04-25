package grpc;

import grpc.client.GrpcClient;
import grpc.server.GrpcServer;
import proto.Request;
import proto.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GrpcClientJavaTestApplicationTests.class)
public class GrpcClientJavaTestApplicationTests {

    private GrpcClient grpcClient;

    private GrpcServer grpcServer;

    private int WAIT_TIME = 24 * 60 * 60 * 1000;

    private int SERVER_PORT = 8000;

    private String CLIENT_IP = "127.0.0.1";

    private int CLIENT_PORT = 8000;

    @Test
    public void contextLoads() throws InterruptedException {
        startServer();
        grpcClient = new GrpcClient(CLIENT_IP, CLIENT_PORT);
        int count = 0;
        while (true) {
            count++;
            Request request = Request.newBuilder().setRequest1("test-" + count).setRequest2("test-" + count).build();
            log.info("构建客户端请求参数，request = [{}]", request.toString());
            if (request != null) {
                log.info("客户端第[{}]次调用服务端...", count);
                Thread.sleep(5000);

                Result result = grpcClient.run(o -> o.method(request));
                log.info("客户端成功获取到服务端结果，result = [{}]", result.toString());
            }
        }
    }

    @Test
    public void startServer() {
        grpcServer = new GrpcServer(SERVER_PORT);
        try {
            grpcServer.start();
            while (true) {
                Thread.sleep(WAIT_TIME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void startClient() {
        grpcClient = new GrpcClient(CLIENT_IP, CLIENT_PORT);
        log.info("JAVA客户端构造请求参数，ip = [{}], port = [{}]", CLIENT_IP, CLIENT_PORT);
        Request request = Request.newBuilder().setRequest1("java-request1").setRequest2("java-request2").build();
        Result result = grpcClient.run(stub -> stub.method(request));
        log.info("JAVA客户端请求服务端，返回结果，result = [{}]", result.toString());
    }


}