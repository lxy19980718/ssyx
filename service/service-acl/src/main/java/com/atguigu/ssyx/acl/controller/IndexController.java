package com.atguigu.ssyx.acl.controller;

import com.atguigu.ssyx.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController //在Spring中注册 并返回Json数据
@RequestMapping("/admin/acl/index")
@Api(tags = "登录")
public class IndexController {

    //login登录
    @ApiOperation("登录")
    @PostMapping("/login")
    public Result login(){
        //返回tocken值
        Map<String,String> map = new HashMap<>();
        map.put("token","token-admin");
        return Result.ok(map);
    }

    //getInfo获取信息
    @ApiOperation("获取信息")
    @GetMapping("/info")
    public Result info(){
        Map<String, String> map = new HashMap<>();
        map.put("name","admin");
        map.put("avator","");
        return Result.ok(map);
    }

    //logout退出
    //getInfo获取信息
    @ApiOperation("退出")
    @PostMapping("/logout")
    public Result logout(){
        return Result.ok(null);
    }

}
