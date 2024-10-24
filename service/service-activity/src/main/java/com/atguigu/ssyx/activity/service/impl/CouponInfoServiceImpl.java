package com.atguigu.ssyx.activity.service.impl;


import com.atguigu.ssyx.activity.mapper.CouponInfoMapper;
import com.atguigu.ssyx.activity.mapper.CouponRangeMapper;
import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.enums.CouponRangeType;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.model.activity.CouponRange;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券信息 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2024-02-11
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private CouponRangeMapper couponRangeMapper;

    @Override
    public IPage<CouponInfo> getCouponPage(Page pageParam) {
        IPage<CouponInfo> page = this.page(pageParam);
        page.getRecords().stream().map(item -> {
            item.setRangeTypeString(item.getRangeType() == null?"":item.getRangeType().getComment());
            item.setCouponTypeString(item.getCouponType().getComment());
            return item;
        }).collect(Collectors.toList());
        return page;
    }

    @Override
    public CouponInfo getCouponInfoById(Long id) {
        CouponInfo couponInfo = this.getById(id);
        couponInfo.setCouponTypeString(couponInfo.getCouponType().getComment());
        if(!ObjectUtils.isEmpty(couponInfo.getRangeType())){
            couponInfo.setRangeTypeString(couponInfo.getRangeType().getComment());
        }
        return couponInfo;
    }

    @Override
    public Map<String, Object> findCouponRuleList(Long id) {
        Map<String, Object> map = new HashMap<>();
        //根据优惠卷id查询优惠券信息
        CouponInfo couponInfo = this.getById(id);
        //根据优惠券id查询coupon_range表里面range_id的值
        List<CouponRange> couponRanges = couponRangeMapper.selectList(Wrappers.<CouponRange>lambdaQuery().eq(CouponRange::getCouponId, id));
        List<Long> couponRangeIds = couponRanges.stream().map(item -> item.getRangeId()).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(couponRangeIds)){

            if(CouponRangeType.SKU.getCode().equals(couponInfo.getRangeType())){
                ////如果当前规则类型 sku  rang_id就是skuId值
                ////如果规则的类型是sku,得到skuid值，通过远程调用根据多个skuid值获取对应的sku信息
                List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoList(couponRangeIds);
                map.put("skuInfoList",skuInfoList);
            }

            ////如果规则类型是 category    rang_id就是分类Id值
            if(CouponRangeType.CATEGORY.getCode().equals(couponInfo.getRangeType())){
                List<Category> categoryList = productFeignClient.findCategoryList(couponRangeIds);
                map.put("categoryList",categoryList);
            }

        }
        return null;
    }

    @Override
    @Transactional
    public Boolean saveCouponRule(CouponRuleVo couponRuleVo) {

        //根据couponId删除coupon_range里的数据
        couponRangeMapper.delete(Wrappers.<CouponRange>lambdaQuery().eq(CouponRange::getCouponId,couponRuleVo.getCouponId()));
        //修改基本信息
        CouponInfo couponInfo = baseMapper.selectById(couponRuleVo.getCouponId());
        BeanUtils.copyProperties(couponRuleVo,couponInfo);
        this.updateById(couponInfo);
        //添加新的规则信息
        couponRuleVo.getCouponRangeList().forEach(item -> {
            item.setCouponId(couponRuleVo.getCouponId());
            couponRangeMapper.insert(item);
        });
        return Boolean.TRUE;
    }
}
