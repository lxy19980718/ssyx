package com.atguigu.ssyx.activity.mapper;

import com.atguigu.ssyx.model.activity.ActivityInfo;
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

    List<Long> selectSkuIdListExist(@Param("ids") List<Long> ids);
}
