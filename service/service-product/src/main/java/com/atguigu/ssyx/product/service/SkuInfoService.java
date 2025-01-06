package com.atguigu.ssyx.product.service;


import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * sku信息 服务类
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
public interface SkuInfoService extends IService<SkuInfo> {

    List<SkuInfo> findNewPersonSkuInfoList();

    IPage<SkuInfo> seletPageSkuInfo(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo);

    void saveSkuInfo(SkuInfoVo skuInfoVo);

    SkuInfoVo getSkuInfo(Long id);

    void updateSkuInfoById(SkuInfoVo skuInfoVo);

    void check(Long skuId, Integer status);

    void publish(Long id, Integer status);

    void isNewPerson(Long skuId, Integer status);

    List<SkuInfo> getBySkuIds(List<Long> skuIds);

    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    SkuInfoVo getSkuInfoVo(Long skuId);
}
