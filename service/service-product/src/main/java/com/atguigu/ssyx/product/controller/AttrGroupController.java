package com.atguigu.ssyx.product.controller;


import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.product.AttrGroup;
import com.atguigu.ssyx.product.service.AttrGroupService;
import com.atguigu.ssyx.vo.product.AttrGroupQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 属性分组 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
@RestController
@Api(tags = "平台属性")
@RequestMapping("admin/product/attrGroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;


    @GetMapping("{page}/{limit}")
    @ApiOperation("平台属性分组列表")
    public Result list(@PathVariable Long page, @PathVariable Long limit, AttrGroupQueryVo attrGroupQueryVo){
        Page<AttrGroup> pageParam = new Page<>(page,limit);
        IPage<AttrGroup> pageModel = attrGroupService.selectPageAttrGroup(pageParam,attrGroupQueryVo);
        return Result.ok(pageModel);
    }


    //查询所有平台属性分组列表
    @GetMapping("findAllList")
    @ApiOperation("查询所有平台属性分组列表")
    public Result findAllList(){
        List<AttrGroup> list = attrGroupService.findAllListAttrGroup();
        return Result.ok(list);
    }

    @ApiOperation("获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        AttrGroup attrGroup = attrGroupService.getById(id);
        return Result.ok(attrGroup);
    }

    @ApiOperation("新增")
    @PostMapping("save")
    public Result save(@RequestBody AttrGroup attrGroup){
    attrGroupService.save(attrGroup);
    return Result.ok(null);
    }

    @ApiOperation("修改")
    @PutMapping("update")
    public Result updateById(@RequestBody AttrGroup attrGroup){
        attrGroupService.updateById(attrGroup);
        return Result.ok(null);
    }


    @ApiOperation("删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        attrGroupService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation("根据id批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        attrGroupService.removeByIds(idList);
        return Result.ok(null);
    }
}
