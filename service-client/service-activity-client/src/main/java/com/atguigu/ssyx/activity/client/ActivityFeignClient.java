package com.atguigu.ssyx.activity.client;

import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.vo.order.CartInfoVo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient("service-activity")
public interface ActivityFeignClient {

    @PostMapping("/api/activity/inner/findActivity")
    public Map<Long, List<String>> findActivity(@RequestBody List<Long> skuIdList);

    @ApiOperation(value = "根据skuId获取优惠券信息")
    @GetMapping("/api/activity/inner/findActivityAndCoupon/{skuId}/{userId}")
    public Map<String, Object> findActivityAndCoupon(@PathVariable Long skuId, @PathVariable Long userId);

    //查询带优惠卷的购物车
    @ApiOperation(value = "查询带优惠卷的购物车")
    @PostMapping("/api/activity/inner/activityCartList")
    OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId);

    /**
     * 获取优惠券范围对应的购物车列表
     *
     * @param cartInfoList
     * @param couponId
     * @return
     */
    @PostMapping(value = "/api/activity/inner/findRangeSkuIdList/{couponId}")
    CouponInfo findRangeSkuIdList(@RequestBody List<CartInfo> cartInfoList, @PathVariable("couponId") Long couponId);

    @PostMapping("/api/activity/inner/findCartActivityList")
    List<CartInfoVo> findCartActivityList(@RequestBody List<CartInfo> cartInfoList);

    //更新优惠卷的使用状态
    @GetMapping("/api/activity/inner/updateCouponInfoUseStatus/{couponId}/{userId}/{orderInfoId}")
    Boolean updateCouponInfoUseStatus(@PathVariable Long couponId,@PathVariable Long userId,@PathVariable Long orderInfoId);
}
