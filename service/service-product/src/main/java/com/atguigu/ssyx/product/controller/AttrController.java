package com.atguigu.ssyx.product.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.product.Attr;
import com.atguigu.ssyx.product.service.AttrService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 商品属性 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
@RestController
@RequestMapping("admin/product/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

     //根据平台属性分组id查询
    @ApiOperation("根据平台属性分组id查询")
    @GetMapping("{groupId}")
    public Result list(@PathVariable Long groupId) {
        List<Attr> list = attrService.getAttrListByGroupId(groupId);
        return Result.ok(list);
    }

    @ApiOperation("id查询")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        Attr attr = attrService.getById(id);
        return Result.ok(attr);
    }

    @ApiOperation("新增")
    @PostMapping("save")
    public Result save(@RequestBody Attr attr){
        attrService.save(attr);
        return Result.ok(null);
    }

    @ApiOperation("修改")
    @PutMapping("update")
    public Result update(@RequestBody Attr attr){
        attrService.updateById(attr);
        return Result.ok(null);
    }


    @ApiOperation("删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        attrService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation("根据id批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        attrService.removeByIds(idList);
        return Result.ok(null);
    }

}

