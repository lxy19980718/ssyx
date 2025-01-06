package com.atguigu.ssyx.activity.service;


import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.vo.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 优惠券信息 服务类
 * </p>
 *
 * @author atguigu
 * @since 2024-02-11
 */
public interface CouponInfoService extends IService<CouponInfo> {



    /**
     * 优惠卷分页查询
     * @param pageParam
     * @return
     */
    IPage<CouponInfo> getCouponPage(Page pageParam);

    CouponInfo getCouponInfoById(Long id);

    Map<String,Object> findCouponRuleList(Long id);

    Boolean saveCouponRule(CouponRuleVo couponRuleVo);

    List<CouponInfo> findCouponInfoList(Long skuId, Long userId);
}
