package com.atguigu.ssyx.product.api;

import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.product.service.CategoryService;
import com.atguigu.ssyx.product.service.SkuInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductInnerController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private SkuInfoService skuInfoService;

    //根据skuid获取分类信息
    @GetMapping("inner/getCategory/{id}")
    public Category getCategory(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    //根据skuid获取sku信息
    @GetMapping("inner/getSkuInfo/{id}")
    public SkuInfo getSkuInfo(@PathVariable Long id){
        return skuInfoService.getById(id);
    }

    //根据skuid列表获取sku信息列表
    @PostMapping("inner/findSkuInfoList")
    public List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuIds){
        return skuInfoService.getBySkuIds(skuIds);
    }

    //根据 categoryId列表获取sku信息列表
    @PostMapping("inner/findCategoryList")
    public List<Category> findCategoryList(@RequestBody List<Long> categoryIds){
        List<Category> categoryList = categoryService.getByCategoryIds(categoryIds);
        return categoryList;
    }

    //根据关键字匹配sku列表
    @GetMapping("inner/findSkuInfoByKeyword/{keyword}")
    public List<SkuInfo> findSkuInfoByKeyword(@PathVariable("keyword") String keyword){
        List<SkuInfo> skuInfoList = skuInfoService.findSkuInfoByKeyword(keyword);
        return skuInfoList;
    }
}
