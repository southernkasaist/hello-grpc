package com.github.southernkasaist.grpc.hello;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class DemoClient {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start running client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
                .build();

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);

        ExecutorService executorService = Executors.newFixedThreadPool(100);

        long start = System.currentTimeMillis();
        List<Future<?>> futures = new ArrayList<>();
        int totalRequests = 10_000;
        for (int i = 0; i < totalRequests; i++) {
            futures.add(
                    executorService.submit(() -> {
                        HelloRequest request = HelloRequest.newBuilder()
                                .setName("guy")
                                .build();

                        HelloReply reply = stub.sayHello(request);
                        reply.getMessage();
                    })
            );
        }
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        long stop = System.currentTimeMillis();
        Duration duration = Duration.ofMillis(stop - start);
        long qps = totalRequests / duration.toSeconds();
        System.out.println("Duration: " + duration.toSecondsPart() + "s " + duration.toMillisPart() + "ms");
        System.out.println("QPS: " + qps);

        executorService.shutdown();

        channel.shutdown();
        channel.awaitTermination(3, TimeUnit.SECONDS);
    }
}
