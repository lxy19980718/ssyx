package com.atguigu.ssyx.activity.service.impl;

import com.atguigu.ssyx.activity.mapper.ActivityInfoMapper;
import com.atguigu.ssyx.activity.mapper.ActivityRuleMapper;
import com.atguigu.ssyx.activity.mapper.ActivitySkuMapper;
import com.atguigu.ssyx.activity.service.ActivityInfoService;
import com.atguigu.ssyx.activity.service.CouponInfoService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.enums.ActivityType;
import com.atguigu.ssyx.model.activity.ActivityInfo;
import com.atguigu.ssyx.model.activity.ActivityRule;
import com.atguigu.ssyx.model.activity.ActivitySku;
import com.atguigu.ssyx.model.activity.CouponInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.ActivityRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 活动表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2024-02-11
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    @Autowired
    private ActivityRuleMapper activityRuleMapper;

    @Autowired
    private ActivitySkuMapper activitySkuMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private CouponInfoService couponInfoService;

    @Override
    public IPage<ActivityInfo> selectPageActivityInfo(Page<ActivityInfo> pageParam) {
        Page<ActivityInfo> activityInfoPage = baseMapper.selectPage(pageParam, null);
        List<ActivityInfo> records = activityInfoPage.getRecords();
        records.stream().forEach(e -> e.setActivityTypeString(ActivityType.getValue(e.getActivityType().getCode())));
        return activityInfoPage;
    }

    @Override
    public Map<String, Object> findActivityRuleList(Long id) {
        Map<String, Object> result = new HashMap<>();
        //1、根据活动id,查询规则列表activity_rule表
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(Wrappers.<ActivityRule>lambdaQuery().eq(ActivityRule::getActivityId,id));
        result.put("activityRuleList",activityRuleList);
        //2、根据活动id查询,查询使用规则商品列表activity_sku表
        List<ActivitySku> activitySkuList = activitySkuMapper.selectList(Wrappers.<ActivitySku>lambdaQuery().eq(ActivitySku::getActivityId,id));
        result.put("activitySkuList",activitySkuList);
        //2.1通过远程调用 service-product模块接口，根据skuid得到商品信息
        List<Long> skuIds = activitySkuList.stream().map(item-> item.getSkuId()).collect(Collectors.toList());
        List<SkuInfo> skuInfoList = Optional.ofNullable(skuIds)
                .map(ids -> productFeignClient.findSkuInfoList(ids))
                .orElse(Collections.emptyList());
        result.put("skuInfoList",skuInfoList);
        return result;
    }

    @Override
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        //1、根据活动id删除之前规则数据
        activityRuleMapper.delete(Wrappers.<ActivityRule>lambdaQuery().eq(ActivityRule::getActivityId, activityRuleVo.getActivityId()));
        activitySkuMapper.delete(Wrappers.<ActivitySku>lambdaQuery().eq(ActivitySku::getActivityId, activityRuleVo.getActivityId()));
        //2、获取规则列表数据
        ActivityInfo activityInfo = baseMapper.selectById(activityRuleVo.getActivityId());
        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();
        activityRuleList.stream().forEach(e -> {
            e.setActivityId(activityRuleVo.getActivityId());
            e.setActivityType(activityInfo.getActivityType());
            activityRuleMapper.insert(e);
        });
        //3、获取规则范围数据
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        activitySkuList.stream().forEach(e -> {
            e.setActivityId(activityRuleVo.getActivityId());
            activitySkuMapper.insert(e);
        });
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        //根据输入关键字去查询sku匹配内容列表
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        //判断：如果根据关键字查询不到匹配内容，直接返回空集合
        if(skuInfoList.size() == 0){
            return skuInfoList;
        }
        List<Long> ids = skuInfoList.stream().map(item -> item.getId()).collect(Collectors.toList());
        //判断商品之前是否参加过活动，如果之前参加过，活动正在进行中
        List<Long> existSkuIdList = baseMapper.selectSkuIdListExist(ids);
        List<SkuInfo> result = skuInfoList.stream().filter(item->!existSkuIdList.contains(item.getId())).collect(Collectors.toList());
        return result;
    }

    @Override
    public Map<Long, List<String>> findActivity(List<Long> skuIdList) {
        Map<Long,List<String>> map = new HashMap<>();
        //skuIdList遍历得到每个skuId
        skuIdList.forEach(skuId -> {
            //根据skuId进行查询，查询sku对应的活动列表
            List<ActivityRule> activityRuleList = baseMapper.findActivityRule(skuId);

            //封装
            if(!CollectionUtils.isEmpty(activityRuleList)){
                List<String> rules = new ArrayList<>();
                //把规则名称处理一下
                for(ActivityRule activityRule:activityRuleList){
                    rules.add(this.getRuleDesc(activityRule));
                }
                map.put(skuId,rules);
            }
        });
        return map;
    }

    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        //1、根据skuid获取sku营销活动，一个活动有多个规则
        Map<String, Object> activityRuleList = this.findActivityRuleList(skuId);

        //2、根据skuId+userId查询优惠券信息
        List<CouponInfo> couponInfoList = couponInfoService.findCouponInfoList(skuId,userId);

        //3、封装
        HashMap<String, Object> map = new HashMap<>();
        map.put("couponInfoList",couponInfoList);
        map.putAll(activityRuleList);
        return map;
    }

    //构造规则名称的方法
    private String getRuleDesc(ActivityRule activityRule) {
        ActivityType activityType = activityRule.getActivityType();
        StringBuffer ruleDesc = new StringBuffer();
        if (activityType == ActivityType.FULL_REDUCTION) {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionAmount())
                    .append("元减")
                    .append(activityRule.getBenefitAmount())
                    .append("元");
        } else {
            ruleDesc
                    .append("满")
                    .append(activityRule.getConditionNum())
                    .append("元打")
                    .append(activityRule.getBenefitDiscount())
                    .append("折");
        }
        return ruleDesc.toString();
    }
}
