package com.atguigu.ssyx.product.service.impl;


import com.atguigu.ssyx.model.product.SkuImage;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.product.mapper.SkuImageMapper;
import com.atguigu.ssyx.product.service.SkuImageService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 商品图片 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
@Service
public class SkuImageServiceImpl extends ServiceImpl<SkuImageMapper, SkuImage> implements SkuImageService {

    @Override
    public List<SkuImage> getSkuImage(Long id) {
        return this.list(Wrappers.<SkuImage>lambdaQuery().eq(SkuImage::getSkuId,id));
    }

    @Override
    public void updateSkuImageBySkuId(SkuInfo skuInfo) {
        this.update(Wrappers.<SkuImage>lambdaUpdate().eq(SkuImage::getSkuId,skuInfo.getId()));
    }
}
