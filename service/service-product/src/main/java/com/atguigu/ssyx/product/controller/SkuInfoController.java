package com.atguigu.ssyx.product.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * sku信息 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
@RestController
@RequestMapping("admin/product/skuInfo")
public class SkuInfoController {

    @Autowired
    private SkuInfoService skuInfoService;

    //SKU列表
    @ApiOperation("sku列表")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, SkuInfoQueryVo skuInfoQueryVo){
        Page<SkuInfo> pageParam = new Page<>(page,limit);
        IPage<SkuInfo> pageModel = skuInfoService.seletPageSkuInfo(pageParam,skuInfoQueryVo);
        return Result.ok(pageModel);
    }


    //添加商品sku信息
    @ApiOperation("添加商品sku信息")
    @PostMapping("save")
    public Result save(@RequestBody SkuInfoVo skuInfoVo){
        skuInfoService.saveSkuInfo(skuInfoVo);
        return Result.ok(null);

    }

    @ApiOperation("根据id查询")
    @GetMapping("get/{id}")
    public Result getById(@PathVariable Long id){
        SkuInfoVo skuInfoVo = skuInfoService.getSkuInfo(id);
        return Result.ok(skuInfoVo);
    }

    @ApiOperation("根据id修改")
    @PutMapping("update")
    public Result update(@RequestBody SkuInfoVo skuInfoVo){
        skuInfoService.updateSkuInfoById(skuInfoVo);
        return Result.ok(null);
    }

    @ApiOperation("根据id删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        skuInfoService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation("批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> ids){
        skuInfoService.removeByIds(ids);
        return Result.ok(null);
    }

    @ApiOperation("商品审核")
    @GetMapping("check/{skuId}/{status}")
    public Result check(@PathVariable Long skuId,@PathVariable Integer status){
        skuInfoService.check(skuId,status);
        return Result.ok(null);
    }

    @ApiOperation("商品上下架")
    @GetMapping("publish/{id}/{status}")
    public Result publish(@PathVariable Long id,@PathVariable Integer status){
        skuInfoService.publish(id,status);
        return Result.ok(null);
    }

    @GetMapping("isNewPerson/{skuId}/{status}")
    public Result isNewPerson(@PathVariable Long skuId,@PathVariable Integer status) {
        skuInfoService.isNewPerson(skuId, status);
        return Result.ok(null);
    }
}

