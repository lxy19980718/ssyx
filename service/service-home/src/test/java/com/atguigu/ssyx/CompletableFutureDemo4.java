package com.atguigu.ssyx;



import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureDemo4 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //串行化
        //1、创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        //2、任务1 返回结果1024
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {

            int value = 1024;
            System.out.println("任务1：" + value);
            return value;
        }, executorService);

        //3、任务2 获取任务1的返回结果
        future.thenApplyAsync((res) -> {
            System.out.println("任务2："+res);
            return res;
        }, executorService);

        //4、任务3 任务1完成之后往下执行
        future.thenRunAsync(()->{
            System.out.println("任务3：");
        },executorService);
    };

}
