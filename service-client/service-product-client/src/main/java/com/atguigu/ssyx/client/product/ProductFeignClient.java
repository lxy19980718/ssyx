package com.atguigu.ssyx.client.product;

import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "service-product")
public interface ProductFeignClient {

    @GetMapping("/api/product/inner/getCategory/{id}")
    public Category getCategory(@PathVariable("id") Long id);


    @GetMapping("/api/product/inner/getSkuInfo/{id}")
    public SkuInfo getSkuInfo(@PathVariable("id") Long id);

    @PostMapping("/api/product/inner/findSkuInfoList")
    public List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuIds);

    //根据关键字匹配sku列表
    @GetMapping("/api/product/inner/findSkuInfoByKeyword/{keyword}")
    public List<SkuInfo> findSkuInfoByKeyword(@PathVariable("keyword") String keyword);

    @GetMapping("api/product/inner/findCategoryList")
    public List<Category> findCategoryList(@RequestBody List<Long> categoryIds);

    //查询所有分类
    @GetMapping("api/product/inner/findAllCategoryList")
    public List<Category> findAllCategoryList();

    //获取新人专享的商品
    @GetMapping("api/product/inner/findNewPersonSkuInfoList")
    public List<SkuInfo> findNewPersonSkuInfoList();

    @GetMapping("api/product/inner/getSkuInfoVo/{skuId}")
    public SkuInfoVo getSkuInfoVo(@PathVariable Long skuId);
}
