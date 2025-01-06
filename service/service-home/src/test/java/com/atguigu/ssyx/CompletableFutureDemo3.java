package com.atguigu.ssyx;



import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureDemo3 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //异步对象结束任务时回调方法
        //1、创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        System.out.println("main begin");
        //2、CompletableFuture创建异步对象
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程"+ Thread.currentThread().getName());
            int value = 1024;
            System.out.println("result："+value);
            return value;
        }, executorService).whenComplete((res,exception)->{
            System.out.println("complete："+res);
        });
        Integer value = future.get();
        System.out.println("main over：");
    }
}
