
# 一，工具准备



1.1，安装brew

```plain
/bin/zsh -c "$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/Homebrew.sh)"
```
1.2，使用brew安装protobuf
```plain
brew install protobuf
```
1.3，查看proto版本
```plain
protoc --version
1.4，安装[protoc-gen-grpc-java](https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.34.1/?fileGuid=pTVt3HYRhwqT6W9d)

```
通过maven配置引入
```plain
<build>
    <extensions>
        <!--动态识别系统版本-->
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.6.2</version>
        </extension>
    </extensions>
    <plugins>
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <!--添加.proto文件的编译工具-->
            <configuration>
                <!--protoc工具通过.proto文件生成对应的java对应的类-->
                <!--os.detected.classifier变量可以根据当前系统的类型来下载对应的工具，注意版本应该与要交互端的proto版本一致-->
                <protocArtifact>com.google.protobuf:protoc:3.15.8:exe:${os.detected.classifier}</protocArtifact>
                <!--protoc-gen-grpc-java工具通过.proto文件生成grpc的工具类-->
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:0.15.0:exe:${os.detected.classifier}</pluginArtifact>
                <!--这是生成grpc工具类存放的文件夹的名字-->
                <pluginId>grpc</pluginId>
                <!--要编辑的.proto文件的路径-->
                <protoSourceRoot>src/main/resources/grpc</protoSourceRoot>
            </configuration>
            <executions>
                <execution>
                    <!--这是上面两个编译工具用到的命令，当用maven编译项目时会执行这两个命令-->
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
# 二，Java maven 依赖

## 2.1，添加依赖

```plain
<properties>
    <maven.compiler.source>8</maven.compiler.source>
    <maven.compiler.target>8</maven.compiler.target>
    <proto.version>1.37.0</proto.version>
</properties>
<dependencies>
    <dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>${proto.version}</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>${proto.version}</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>${proto.version}</version>
</dependency>
<dependency> <!-- necessary for Java 9+ -->
    <groupId>org.apache.tomcat</groupId>
    <artifactId>annotations-api</artifactId>
    <version>6.0.53</version>
    <scope>provided</scope>
</dependency>
<!-- https://mvnrepository.com/artifact/io.grpc/protoc-gen-grpc-java -->
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>protoc-gen-grpc-java</artifactId>
    <version>1.37.0</version>
    <type>pom</type>
</dependency>
</dependencies>
```
# 
# 三，Java grpc类与Java类生成

## 3.1，编写protoc文件

下面是定义一个简单的protobuf grpc文件，service块内定义调用的function，message定义的是结构体。**注意：package和对接端，如python端protoc的package配置必须一致。**

```plain
syntax = "proto3";
package example;
option java_package = "proto";
option java_outer_classname = "GrpcTestServiceProto";
option java_multiple_files = true;
service TestService
{
  rpc method(Request) returns (Result){}
}
message Request
{
  string request1 = 1;
  string request2 = 2;
}
message Result
{
  string result1 = 1;
  string result2 = 2;
}
```
## 3.2，命令行生成

生成java类：**[protobuf exe path]  --java_out=[需要生成java的位置]**

```plain
protoc ./data.proto --java_out=./
```
生成Java grpc类：**[protobuf exe path] --plugin=[protoc-gen-grpc-java exe path] --grpc-java_out=[需要生成java的位置] [proto文件位置]**
```plain
D:\protobuf\bin\protoc --plugin=protoc-gen-grpc-java=D:\protobuf\bin\protoc-gen-grpc-java-1.30.2-windows-x86_64.exe --grpc-java_out=./ ./data.proto
```
## 3.3，maven编译生成

通过1.4中引入的maven插件配置，结合protoc文件，通过maven compile一键生成java和java grpc类，文件目录结构：

![图片](https://uploader.shimo.im/f/V4m78xfdPA6uqByI.png!thumbnail?fileGuid=pTVt3HYRhwqT6W9d)

# 四，Server端编写

文件目录结构

![图片](https://uploader.shimo.im/f/r9aYWCnU3QPT1WmS.png!thumbnail?fileGuid=pTVt3HYRhwqT6W9d)

### 4.1 编写TestServiceGrpcImpl

```java
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
```

### 4.2 编写Server入口类

```java
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
```

# 五，Client端编写

## 5.1，函数式接口封装

```java
/**
 * 函数式接口：封装客户端调用
 *
 * @param <Stub>
 * @param <Result>
 */
public interface Functional<Stub, Result>
{
    Result run(Stub arg);
}
```
## 5.2，编写Client入口类

```java
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
```
# 六，测试类编写

## 6.1，编写测试类

```java
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
```
## 6.2，启动服务端

![图片](https://uploader.shimo.im/f/J3YpdwBckEkDdbxY.png!thumbnail?fileGuid=pTVt3HYRhwqT6W9d)

## 6.3，启动客户端![图片](https://uploader.shimo.im/f/GOqive74Edp0izis.png!thumbnail?fileGuid=pTVt3HYRhwqT6W9d)

## 
