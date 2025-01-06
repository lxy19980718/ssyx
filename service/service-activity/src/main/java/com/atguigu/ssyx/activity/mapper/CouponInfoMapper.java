package com.atguigu.ssyx.activity.mapper;


import com.atguigu.ssyx.model.activity.CouponInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 优惠券信息 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2024-02-11
 */
@Repository
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {

    List<CouponInfo> selectCouponInfoList(@Param("skuId") Long skuId, @Param("categoryId")Long categoryId, @Param("userId")Long userId);
}
