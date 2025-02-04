package com.atguigu.ssyx.activity.api;

import com.atguigu.ssyx.activity.service.ActivityInfoService;
import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.vo.order.CartInfoVo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
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

    @Autowired
    private CouponInfoService couponInfoService;

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

    @ApiOperation(value = "获取购物车里面满足条件的优惠券和活动的信息")
    @PostMapping("inner/activityCartList/{userId}")
    public OrderConfirmVo findCartActivityAndCoupon(@RequestBody List<CartInfo> cartInfoList,@PathVariable Long userId){
        return activityInfoService.findCartActivityAndCoupon(cartInfoList,userId);
    }

    @PostMapping("inner/findCartActivityList")
    public List<CartInfoVo> findCartActivityList(@RequestBody List<CartInfo> cartInfoList){
        return activityInfoService.findCartActivityList(cartInfoList);
    }

    //获取购物车对应优惠卷的部分
    @PostMapping(value = "inner/findRangeSkuIdList/{couponId}")
    CouponInfo findRangeSkuIdList(@RequestBody List<CartInfo> cartInfoList, @PathVariable("couponId") Long couponId){
        return couponInfoService.findRangeSkuIdList(cartInfoList,couponId);
    };
}
