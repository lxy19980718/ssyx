package com.atguigu.ssyx.activity.api;

import com.atguigu.ssyx.activity.service.ActivityInfoService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityInfoApiController {

    @Autowired
    private ActivityInfoService activityInfoService;

    @ApiOperation(value = "根据skuIds获取促销信息")
    @PostMapping("inner/findActivity")
    public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList){
        return activityInfoService.findActivity(skuIdList);
    }

    @ApiOperation(value = "根据skuId获取优惠券信息")
    @GetMapping("inner/findActivityAndCoupon/{skuId}/{userId}")
    public Map<String, Object> findActivityAndCoupon(@PathVariable Long skuId,@PathVariable Long userId){
        return activityInfoService.findActivityAndCoupon(skuId,userId);
    }
}
