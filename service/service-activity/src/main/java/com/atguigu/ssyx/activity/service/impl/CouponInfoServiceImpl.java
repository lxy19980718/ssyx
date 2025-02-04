package com.atguigu.ssyx.activity.service.impl;


import com.atguigu.ssyx.activity.mapper.CouponInfoMapper;
import com.atguigu.ssyx.activity.mapper.CouponRangeMapper;
import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.enums.CouponRangeType;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.model.activity.CouponRange;
import com.atguigu.ssyx.model.order.CartInfo;
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

import java.math.BigDecimal;
import java.util.*;
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

    @Override
    public List<CouponInfo> findCouponInfoList(Long skuId, Long userId) {
        //根据skuId 获取skuInfo信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        //根据skuId categoryId userId 查询
        List<CouponInfo> CouponInfoList = baseMapper.selectCouponInfoList(skuId,skuInfo.getCategoryId(),userId);

        return CouponInfoList;
    }

    @Override
    public List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId) {
        //  1、根据userId获取用户全部优惠卷
        // coupon_use coupon_info
        List<CouponInfo> couponInfoList = baseMapper.selectCouponInfoListByUserId(userId);
        if(CollectionUtils.isEmpty(couponInfoList)){
            return null;
        }

        // 2、从第一步返回的list集合中，获取所有优惠卷id
        List<Long> couponIds = couponInfoList.stream().map(CouponInfo::getId).collect(Collectors.toList());

        // 3、查询优惠卷使用的范围 coupon_range
        List<CouponRange> couponRanges = couponRangeMapper.selectList(Wrappers.<CouponRange>lambdaQuery()
                .in(CouponRange::getCouponId, couponIds));

        // 4、获取优惠卷id对应skuId的列表
        //map<Long,List<skuId>>
        Map<Long,List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList,couponRanges);

        // 5、遍历该优惠券全部优惠卷集合，判断优惠卷类型
        BigDecimal reduceAmount = new BigDecimal("0");
        CouponInfo optimalCouponInfo = null;
        for (CouponInfo couponInfo:couponInfoList){
            //全场通用
            if(CouponRangeType.ALL == couponInfo.getRangeType()){
                //全场通用
                //判断是否满足优惠使用门槛
                //计算购物车商品的总价
                BigDecimal totalAmout = computeTotalAmount(cartInfoList);
                if(totalAmout.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            } else {
                List<Long> skuIds = couponIdToSkuIdMap.get(couponInfo.getId());
                //满足使用范围的购物项
                List<CartInfo> currentCartInfoList = cartInfoList.stream().filter(cartInfo -> skuIds.contains(cartInfo.getSkuId()))
                        .collect(Collectors.toList());
                BigDecimal totalAmount = computeTotalAmount(currentCartInfoList);
                if(totalAmount.subtract(couponInfo.getConditionAmount()).doubleValue() >= 0){
                    couponInfo.setIsSelect(1);
                }
            }
            if (couponInfo.getIsSelect().intValue() == 1 && couponInfo.getAmount().subtract(reduceAmount).doubleValue() > 0) {
                reduceAmount = couponInfo.getAmount();
                optimalCouponInfo = couponInfo;
            }
        }

        if(null != optimalCouponInfo) {
            optimalCouponInfo.setIsOptimal(1);
        }
        return couponInfoList;
    }

    @Override
    public CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId) {
        //根据优惠卷id查询基本信息
        CouponInfo couponInfo = baseMapper.selectById(couponId);
        if(couponInfo == null){
            return null;
        }
        List<CouponRange> couponRanges = couponRangeMapper.selectList(Wrappers.<CouponRange>lambdaQuery().eq(CouponRange::getCouponId, couponId));

        //对应的sku信息
        Map<Long, List<Long>> couponIdToSkuIdMap = this.findCouponIdToSkuIdMap(cartInfoList, couponRanges);
        couponIdToSkuIdMap.values().forEach(skuIds -> {
            couponInfo.setSkuIdList(skuIds);
        });
        return couponInfo;
    }

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo:cartInfoList) {
            if(cartInfo.getIsChecked() == 1){
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }

        }
        return total;
    }

    private Map<Long, List<Long>> findCouponIdToSkuIdMap(List<CartInfo> cartInfoList, List<CouponRange> couponRanges) {
 
        Map<Long, List<Long>> couponIdToSkuIdMap = new HashMap<>();
        //couponRanges数据处理 根据优惠卷id分组
        Map<Long, List<CouponRange>> couponIdToCouponRange = couponRanges.stream().collect(Collectors.groupingBy(CouponRange::getCouponId));

        couponIdToCouponRange.forEach((couponId,couponRangeList) -> {
            Set<Long> skuIds = new HashSet<>();
            //RangeId是skuId或者categoryId
            for(CartInfo cartInfo:cartInfoList) {
                for (CouponRange couponRange: couponRangeList){
                    //判断
                    if(couponRange.getRangeType() == CouponRangeType.SKU && couponRange.getRangeId().longValue() == cartInfo.getSkuId().longValue()){
                        //商品类型的优惠
                        skuIds.add(cartInfo.getSkuId());
                    } else if (couponRange.getRangeType() == CouponRangeType.CATEGORY && couponRange.getRangeId().longValue() == cartInfo.getCategoryId().longValue()){
                        //分类类型的优惠
                        skuIds.add(cartInfo.getSkuId());
                    } else {

                    }
                }
                couponIdToSkuIdMap.put(couponId,new ArrayList<>(skuIds));
            }

        });
        return couponIdToSkuIdMap;
    }
}
