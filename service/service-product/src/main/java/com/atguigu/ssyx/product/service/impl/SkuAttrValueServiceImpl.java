package com.atguigu.ssyx.product.service.impl;


import com.atguigu.ssyx.model.product.SkuAttrValue;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.product.mapper.SkuAttrValueMapper;
import com.atguigu.ssyx.product.service.SkuAttrValueService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * spu属性值 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
@Service
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValue> implements SkuAttrValueService {
    @Override
    public List<SkuAttrValue> getSkuAttrValue(Long id) {
        return this.list(Wrappers.<SkuAttrValue>lambdaQuery().eq(SkuAttrValue::getSkuId,id));
    }

    @Override
    public void updateSkuAttrValueBySkuId(SkuInfo skuInfo) {
        this.list(Wrappers.<SkuAttrValue>lambdaUpdate().eq(SkuAttrValue::getSkuId,skuInfo.getId()));
    }

}
