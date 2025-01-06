package com.atguigu.ssyx.home.controller;

import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.home.service.HomeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "首页接口")
@RestController
@RequestMapping("api/home")
public class HomeApiController {

    @Autowired
    HomeService homeService;

    @ApiOperation(value = "首页数据显示 ")
    @GetMapping("index")
    public Result index(HttpServletRequest request){
        //现在是在当前线程中获取userId，也可以在request中获取
        Long userId = AuthContextHolder.getUserId();
        Map<String,Object> map = homeService.homeData(userId);
        return Result.ok(map);
    }

}
