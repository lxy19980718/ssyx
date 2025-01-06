package com.atguigu.ssyx.home.service.impl;

import com.atguigu.ssyx.activity.client.ActivityFeignClient;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.client.search.SearchFeignClient;
import com.atguigu.ssyx.home.service.ItemService;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService  {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private SearchFeignClient searchFeignClient;

    @Resource
    private ActivityFeignClient activityFeignClient;

    @Override
    public Map<String, Object> item(Long skuId, Long userId) {

        //1、sku信息
        Map<String, Object> result = new HashMap<>();

        //skuId查询
        CompletableFuture<SkuInfoVo> skuFuture = CompletableFuture.supplyAsync(() -> {
            // 远程调用获取sku对应的数据
            SkuInfoVo skuInfoVo = productFeignClient.getSkuInfoVo(skuId);
            result.put("skuInfoVo", skuInfoVo);
            return skuInfoVo;

        },threadPoolExecutor);

        //2、获取sku对用优惠券信息
        CompletableFuture<Map<String, Object>> activityFuture = CompletableFuture.supplyAsync(() -> {
            Map<String, Object> activityMap = activityFeignClient.findActivityAndCoupon(skuId, userId);
            result.putAll(activityMap);
            return activityMap;

        },threadPoolExecutor);

        //3、更新sku商品热度(更新es里面的数据)
        CompletableFuture<Boolean> hotFuture = CompletableFuture.supplyAsync(() -> {
            Boolean aBoolean = searchFeignClient.incrHotScore(skuId);
            return aBoolean;
        },threadPoolExecutor);

        //任务合并
        CompletableFuture.allOf(skuFuture,activityFuture,hotFuture).join();
        return result;
    }
}
