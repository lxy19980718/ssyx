package com.atguigu.ssyx.activity.mapper;

import com.atguigu.ssyx.model.activity.ActivityInfo;
import com.atguigu.ssyx.model.activity.ActivityRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 活动表 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2024-02-11
 */
@Repository
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {

    //如果之前参加过，活动正在进行中，排除商品
    List<Long> selectSkuIdListExist(@Param("ids") List<Long> ids);

    //根据skuId进行查询，查询sku对应活动里面的规则列表
    List<ActivityRule> findActivityRule(@Param("skuId") Long skuId);
}
