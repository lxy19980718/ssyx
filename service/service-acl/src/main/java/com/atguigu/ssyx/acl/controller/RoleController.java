package com.atguigu.ssyx.acl.controller;


import com.atguigu.ssyx.acl.service.RoleService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.acl.Role;
import com.atguigu.ssyx.vo.acl.RoleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "角色接口")
@RestController
@RequestMapping("/admin/acl/role")
public class RoleController {

    //注入Service
    @Autowired
    private RoleService roleService;

    //角色列表（条件分页查询）
    @ApiOperation("角色条件分页查询")
    @GetMapping("{current}/{limit}")
    public Result pageList(@PathVariable Long current, @PathVariable Long limit, RoleQueryVo roleQueryVo){

        //创建page对象，传递当前页和每页记录数
        //current：当前页
        //limit：每页显示记录数
        Page<Role> pageParam = new Page<>(current,limit);

        //调用service 方法实现条件分页查询，返回分页对象
        IPage<Role> ipageModel = roleService.selectRolePage(pageParam,roleQueryVo);
        //调用service里面的方法实现条件分页查询，返回分页对象
        return Result.ok(ipageModel);
    }

    //根据id查询角色
    @ApiOperation("根据id查询角色")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id){
        Role role = roleService.getById(id);
        return Result.ok(role);
    }

    //添加角色
    //@RequestBody 接收JSON格式数据  封装到对象中去
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody Role role){
        boolean isSuccess = roleService.save(role);
        if(isSuccess){
            return Result.ok(null);
        }else{
            return Result.fail(null);
        }
    }

    //修改角色
    @ApiOperation("修改角色")
    @PutMapping("/update")
    public Result update(@RequestBody Role role){
        boolean isUpdate = roleService.updateById(role);
        if(isUpdate){
            return Result.ok(null);
        }else{
            return Result.fail(null);
        }
    }

    //根据id删除角色
    @ApiOperation("根据id删除角色")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id){
        roleService.removeById(id);
        return Result.ok(null);
    }

    //批量删除角色
    @Transactional
    @ApiOperation("批量删除角色")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> ids){
        roleService.removeByIds(ids);
        return Result.ok(null);
    }





}
