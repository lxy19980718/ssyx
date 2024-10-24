package com.atguigu.ssyx.product.service.impl;


import com.atguigu.ssyx.model.product.Attr;
import com.atguigu.ssyx.product.mapper.AttrMapper;
import com.atguigu.ssyx.product.service.AttrService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 商品属性 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
@Service
public class AttrServiceImpl extends ServiceImpl<AttrMapper, Attr> implements AttrService {

    @Override
    public List<Attr> getAttrListByGroupId(Long groupId) {
        return baseMapper.selectList(Wrappers.<Attr>lambdaQuery().eq(Attr::getAttrGroupId,groupId));

    }
}
