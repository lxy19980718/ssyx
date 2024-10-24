package com.atguigu.ssyx.activity.controller;


import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.vo.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 优惠券信息 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2024-02-11
 */
@RestController
@RequestMapping("admin/activity/couponInfo")
public class CouponInfoController {
    @Autowired
    private CouponInfoService couponInfoService;

    /**
     * 优惠卷分页查询
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/{page}/{limit}")
    public Result getCouponPage(@PathVariable("page") Long page,@PathVariable("limit") Long limit){
        Page pageParam = new Page(page, limit);
        return Result.ok(couponInfoService.getCouponPage(pageParam));
    }


    /**
     * 新增
     * @param couponInfo
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody CouponInfo couponInfo){
        return Result.ok(couponInfoService.save(couponInfo));
    }

    /**
     * 根据id查询优惠卷
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public Result getById(@PathVariable("id") Long id){
        return Result.ok(couponInfoService.getCouponInfoById(id));
    }

    /**
     * 根据优惠卷id查询规则数据
     * @param id
     * @return
     */
    @GetMapping("/findCouponRuleList/{id}")
    public Result findCouponRuleList(@PathVariable("id") Long id){
        return Result.ok(couponInfoService.findCouponRuleList(id));
    }

    //添加优惠卷规则数据
    @PostMapping("/saveCouponRule")
    public Result saveCouponRule(@RequestBody CouponRuleVo couponRuleVo) {
        return Result.ok(couponInfoService.saveCouponRule(couponRuleVo));
    }

    /**
     * 更新
     * @param couponInfo
     * @return
     */
    @PutMapping("/update")
    public Result update(@RequestBody CouponInfo couponInfo){
        return Result.ok(couponInfoService.update(Wrappers.update(couponInfo)));
    }

    /**
     *批量删除
     * @param ids
     * @return
     */
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> ids){
        return Result.ok(couponInfoService.removeByIds(ids));
    }

    @DeleteMapping("/remove/{id}")
    public Result removeById(@PathVariable("id") Long id){
        return  Result.ok(couponInfoService.removeById(id));
    }

}

