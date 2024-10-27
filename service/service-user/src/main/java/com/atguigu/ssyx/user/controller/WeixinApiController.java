package com.atguigu.ssyx.user.controller;

import com.atguigu.ssyx.common.result.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/weixin")
public class WeixinApiController {

    //用户微信授权登录
    @ApiOperation("微信登录获取openId(小程序)")
    @GetMapping("/wxLogin/{code}")
    public Result loginWx(@PathVariable String code){


        return Result.ok(null);
    }
}
