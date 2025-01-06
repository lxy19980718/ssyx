package com.atguigu.ssyx;



import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureDemo5 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //任务合并
        //1、创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        //2、任务1 返回结果1024
        CompletableFuture<Integer> futureA = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "：begining...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int value = 1024;
            System.out.println("任务1：" + value);
            System.out.println(Thread.currentThread().getName() + "：end...");
            return value;
        }, executorService);

        //2、任务1 返回结果200
        CompletableFuture<Integer> futureB = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "：begining...");
            int value = 1024;
            System.out.println("任务2：" + value);
            System.out.println(Thread.currentThread().getName() + "：end...");
            return value;
        }, executorService);

        //4、任务3 任务1完成之后往下执行
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futureA, futureB);
        allFuture.get();
        System.out.println("over......");
    };
}
