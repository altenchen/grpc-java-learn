package grpc.server;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import proto.Request;
import proto.Result;
import proto.TestServiceGrpc;

/**
 * @ProjectName: grpc-learn
 * @Date: 2021/4/25 10:40 上午
 * @Author: altenchen
 * @Description: grpc接口实现
 */
@Slf4j
public class TestServiceGrpcImpl extends TestServiceGrpc.TestServiceImplBase {

    @Override
    public void method(Request request, StreamObserver<Result> responseObserver) {
        String res1 = "";
        String res2 = "";
        if (request.getRequest1().equals("py-request1")) {
            res1 = "res1-py-grpc.client->java-grpc.server";
            res2 = "res2-py-grpc.client->java-grpc.server";
        } else if (request.getRequest1().equals("java-request1")){
            res1 = "res1-java-grpc.client->java-grpc.server";
            res2 = "res2-java-grpc.client->java-grpc.server";
        }

        Result result = Result.newBuilder().setResult1(res1).setResult2(res2).build();
        log.info("JAVA服务端返回结果，result = [{}]", result.toString());
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }
}
