package com.atguigu.ssyx;



import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //runAsync 方法，创建异步对象CompletableFuture

        //1、创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        System.out.println("main begin");
        //2、CompletableFuture创建异步对象
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程"+ Thread.currentThread().getName());
            int value = 1024;
            System.out.println("result"+value);
            return value;
        }, executorService);
        Integer value = future.get();
        System.out.println("main over："+value);
    }
}
