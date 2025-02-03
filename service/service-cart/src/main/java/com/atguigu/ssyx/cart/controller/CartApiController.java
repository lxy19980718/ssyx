package com.atguigu.ssyx.cart.controller;

import com.atguigu.ssyx.activity.client.ActivityFeignClient;
import com.atguigu.ssyx.cart.service.CartInfoService;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartInfoService cartInfoService;


    @Autowired
    private ActivityFeignClient activityFeignClent;

    //购物车列表
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.cartList(userId);
        return Result.ok(cartInfoList);
    }

    //添加商品
    //添加内容：当前登录用户id skuId 商品数量
    @GetMapping("addToCart/{skuId}/{skuNum}")
    @ApiOperation("加入购物车")
    public Result addToCart(@PathVariable("skuId") Long skuId,@PathVariable("skuNum") Integer skuNum ){
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.addToCart(userId,skuId,skuNum);
        return Result.ok(null);
    }


    //根据skuId删除购物车
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId){
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteCart(skuId,userId);
        return Result.ok(null);
    }

    //清空购物车
    @DeleteMapping("deleteAllCart")
    public Result deleteAllCart(){
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.deleteAllCart(userId);
        return Result.ok(null);
    }

    //批量删除购物车skuId
    @DeleteMapping("batchDeleteCart")
    public Result batchDeleteCart(@RequestBody List<Long> skuIds){
        Long userId = AuthContextHolder.getUserId();
        cartInfoService.batchDeleteCart(userId,skuIds);
        return Result.ok(null);
    }

    //查询带优惠卷的购物车
    @GetMapping("activityCartList")
    public Result activityCartList(){
        Long userId = AuthContextHolder.getUserId();
        List<CartInfo> cartInfoList = cartInfoService.cartList((userId));

        OrderConfirmVo orderConfirmVo = activityFeignClent.findCartActivityAndCoupon(cartInfoList, userId);
        return Result.ok(orderConfirmVo);
    }
}
