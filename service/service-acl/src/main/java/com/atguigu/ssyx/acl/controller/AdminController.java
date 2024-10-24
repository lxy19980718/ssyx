
package com.atguigu.ssyx.acl.controller;

import com.atguigu.ssyx.acl.service.AdminService;
import com.atguigu.ssyx.acl.service.RoleService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.utils.MD5;
import com.atguigu.ssyx.model.acl.Admin;
import com.atguigu.ssyx.vo.acl.AdminQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "用户接口")
@RequestMapping("/admin/acl/user")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private RoleService roleService;

    //为用户进行分配角色
    @ApiOperation("为用户进行分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestParam Long adminId,@RequestParam List<Long> roleId){
    roleService.saveAdminRole(adminId,roleId);
    return Result.ok(null);
    }


    //获取用户所有角色，和根据用户id查询用户分配的角色列表
    @ApiOperation("获取用户所有角色")
    @GetMapping("/toAssign/{adminId}")
    public Result toAssign(@PathVariable Long adminId){
       Map<String,Object> map = roleService.getRoleByAdminId(adminId);
       //返回的map集合中会包含两部分数据：所有角色 为用户已经分配过的角色列表
       return Result.ok(map);
    }

    //用户列表
    @ApiOperation("用户列表")
    @GetMapping("/{current}/{limit}")
    public Result List(@PathVariable Long current, @PathVariable Long limit, AdminQueryVo adminQueryVo){
        Page<Admin> pageParam = new Page<Admin>(current,limit);
        IPage<Admin> ipage =  adminService.selectPageUser(pageParam,adminQueryVo);
        return Result.ok(ipage);
    }

    //id查询用户
    @ApiOperation("根据id查询")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long  id){
        Admin admin = adminService.getById(id);
        return Result.ok(admin);
    }

    //添加用户
    @ApiOperation("添加用户")
    @PostMapping("/save")
    public Result save(@RequestBody Admin admin){
        //获取输入的密码
        String password = admin.getPassword();
        //对输入的密码进行加密 MD5
        String passwordMD5 = MD5.encrypt(password);
        //设置到admin对象里面
        admin.setPassword(passwordMD5);
        adminService.save(admin);
        return Result.ok(null);
    }

    //修改用户
    @ApiOperation("修改用户")
    @PutMapping("/update")
    public Result update(@RequestBody Admin admin){
        adminService.updateById(admin);
        return Result.ok(null);
    }

    //id删除用户
    @ApiOperation("id删除用户")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id){
        adminService.removeById(id);
        return Result.ok(null);
    }

    //批量删除
    @ApiOperation("批量删除")
    @DeleteMapping("/bathRemove")
    public Result bathRemove(@RequestBody List<Long> ids){
        adminService.removeByIds(ids);
        return Result.ok(ids);
    }
}
