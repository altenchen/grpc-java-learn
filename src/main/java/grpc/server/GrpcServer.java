package grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GrpcServer {

    private final int port;

    private final Server server;

    public GrpcServer(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port).addService(new TestServiceGrpcImpl()).build();
    }

    public void start() throws IOException {
        server.start();
        log.info("启动JAVA服务端, ip = [{}], port = [{}]", Inet4Address.getLocalHost(), port);
        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            try {
                GrpcServer.this.stop();
            } catch (InterruptedException e) {
                log.error("停止JAVA服务端失败...");
                e.printStackTrace();
            }
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }



}