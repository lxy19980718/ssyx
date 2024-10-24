package com.atguigu.ssyx.sys.service.impl;


import com.atguigu.ssyx.model.sys.Region;
import com.atguigu.ssyx.sys.mapper.RegionMapper;
import com.atguigu.ssyx.sys.service.RegionService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 地区表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-10-31
 */
@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements RegionService {

    @Override
    public List<Region> getRegionByKeyword(String keyword) {
        List<Region> list = baseMapper.selectList(Wrappers.<Region>lambdaQuery().like(Region::getName, keyword));
        return list;
    }
}
