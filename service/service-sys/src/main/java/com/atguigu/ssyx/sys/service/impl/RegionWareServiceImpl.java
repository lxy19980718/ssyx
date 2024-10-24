package com.atguigu.ssyx.sys.service.impl;


import com.atguigu.ssyx.common.exception.SsyxException;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.model.sys.RegionWare;
import com.atguigu.ssyx.sys.mapper.RegionWareMapper;
import com.atguigu.ssyx.sys.service.RegionWareService;
import com.atguigu.ssyx.vo.sys.RegionWareQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 城市仓库关联表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-10-31
 */
@Service
public class RegionWareServiceImpl extends ServiceImpl<RegionWareMapper, RegionWare> implements RegionWareService {

    @Override
    public IPage<RegionWare> selectPageRegionWare(Page<RegionWare> pageParam, RegionWareQueryVo regionWareQueryVo) {
        //1、获取条件查询值
        String keyword = regionWareQueryVo.getKeyword();

        //2、判断条件值是否为空，不为空封装条件
        IPage<RegionWare> regionWarePage = baseMapper.selectPage(pageParam,Wrappers.<RegionWare>lambdaQuery().like(RegionWare::getRegionName, keyword).or().like(RegionWare::getWareName, keyword));
        return  regionWarePage;

    }

    @Override
    public void saveRegionWare(RegionWare regionWare) {
        //判断区域是否已经开通了
        Integer count = baseMapper.selectCount(Wrappers.<RegionWare>lambdaQuery().eq(RegionWare::getRegionId,regionWare.getRegionId()));
        //已经存在
        if(count > 0){
            throw new SsyxException(ResultCodeEnum.REGION_OPEN);
        }
        baseMapper.insert(regionWare);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        RegionWare regionWare = baseMapper.selectById(id);
        regionWare.setStatus(status);
        baseMapper.updateById(regionWare);
    }
}
