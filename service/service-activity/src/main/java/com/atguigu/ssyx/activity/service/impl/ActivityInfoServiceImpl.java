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
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.ActivityRuleVo;
import com.atguigu.ssyx.vo.order.CartInfoVo;
import com.atguigu.ssyx.vo.order.OrderConfirmVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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

    @Autowired
    private RedisTemplate redisTemplate;

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
        List<ActivityRule> activityAndRuleList = this.findActivityAndRuleList(skuId);

        //2、根据skuId+userId查询优惠券信息
        List<CouponInfo> couponInfoList = couponInfoService.findCouponInfoList(skuId,userId);

        //3、封装
        HashMap<String, Object> map = new HashMap<>();
        map.put("couponInfoList",couponInfoList);
        map.put("activityRuleList",activityAndRuleList);
        return map;
    }

    @Override
    public List<ActivityRule> findActivityAndRuleList(Long skuId) {
        List<ActivityRule> activityRuleList = baseMapper.findActivityRule(skuId);
        for(ActivityRule activityRule : activityRuleList){
            String ruleDesc = this.getRuleDesc(activityRule);
            activityRule.setRuleDesc(ruleDesc);
        }
        return activityRuleList;
    }

    @Override
    public OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {

        //1、获取购物车 每个商品参与的活动规则，根据活动规则进行分组
        //1个规则对应多个商品  1个商品只能参与一个活动
        List<CartInfoVo> cartInfoVoList = this.findCartActivityList(cartInfoList);


        //2、计算商品参与活动之后金额
        BigDecimal activityReduceAmount = cartInfoVoList.stream().filter(cartInfoVo -> cartInfoVo.getActivityRule() != null)
                .map(cartInfoVo -> cartInfoVo.getActivityRule().getReduceAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //3、获取购物车可以使用的优惠卷列表
        List<CouponInfo> couponInfoList = couponInfoService.findCartCouponInfo(cartInfoList,userId);



        //4、计算商品使用优惠卷之后的金额，一次只能使用一张优惠卷
        BigDecimal couponReduceAmount = new BigDecimal(0);
        if(!CollectionUtils.isEmpty(couponInfoList)) {
             couponReduceAmount = couponInfoList.stream()
                    .filter(couponInfo -> couponInfo.getIsOptimal().intValue() == 1)
                    .map(CouponInfo::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }


        //5、计算没有参与活动，没有使用优惠卷的金额
        BigDecimal originalTotalAmount = cartInfoList.stream().filter(cartInfo -> cartInfo.getIsChecked() == 1)
                .map(cartInfo -> cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //6、最终支付金额 = 总金额  - 活动优惠金额 - 优惠卷金额
        BigDecimal totalAmount = originalTotalAmount.subtract(activityReduceAmount).subtract(couponReduceAmount);


        //7、封装数据
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        orderConfirmVo.setCarInfoVoList(cartInfoVoList);
        orderConfirmVo.setActivityReduceAmount(activityReduceAmount);
        orderConfirmVo.setCouponInfoList(couponInfoList);
        orderConfirmVo.setCouponReduceAmount(couponReduceAmount);
        orderConfirmVo.setOriginalTotalAmount(originalTotalAmount);
        orderConfirmVo.setTotalAmount(totalAmount);
        return orderConfirmVo;
    }

    @Override
    public List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList) {
        List<CartInfoVo> cartInfoVos = new ArrayList<>();
        List<Long> skuIds = cartInfoList.stream().map(cartInfo -> cartInfo.getSkuId()).collect(Collectors.toList());
        List<ActivitySku> activitySkus = baseMapper.selectCartActivity(skuIds);
        //根据activityId分组,每个activityId有哪些skuId
        Map<Long, Set<Long>> activityIdToskuIdsMap = activitySkus.stream()
                .collect(Collectors.groupingBy(ActivitySku::getActivityId, Collectors.mapping(ActivitySku::getSkuId, Collectors.toSet())));

        //获取活动里面规则的数据  key:活动id value：活动规则列表
        Map<Long,List<ActivityRule>> activityRuleMap = new HashMap<>();
        
        //活动id
        Set <Long> activityIds = activitySkus.stream().map(ActivitySku::getActivityId).collect(Collectors.toSet());

        if(!CollectionUtils.isEmpty(activityIds)) {
            //activity_rule表
            List<ActivityRule> activityRuleList = activityRuleMapper.selectList(Wrappers.<ActivityRule>lambdaQuery()
                    .in(ActivityRule::getActivityId, activityIds)
                    .orderByDesc(ActivityRule::getConditionAmount,ActivityRule::getConditionNum));
            //封装到activityRuleMap中
            activityRuleMap = activityRuleList.stream().collect(Collectors.groupingBy(ActivityRule::getActivityId));
        }

        //有活动的购物向skuId
        Set<Long> activitySkuIdSet = new HashSet<>();
        if(!CollectionUtils.isEmpty(activityIdToskuIdsMap)) {
            Map<Long, List<ActivityRule>> finalActivityRuleMap = activityRuleMap;
            AtomicReference<ActivityRule> activityRule = null;
            activityIdToskuIdsMap.forEach((k, v) -> {
                List<CartInfo> infos = cartInfoList.stream().filter(cartInfo -> v.contains(cartInfo.getSkuId())).collect(Collectors.toList());
                //计算购物项总金额和总数量
                BigDecimal totalAmount = this.computeTotalAmount(infos);
                int cartNum = this.computeCartNum(infos);

                //计算活动对应规则
                //根据activityId获取活动对应规则
                List<ActivityRule> activityRuleList = finalActivityRuleMap.get(k);

                ActivityType activityType = activityRuleList.get(0).getActivityType();
                if(activityType == ActivityType.FULL_REDUCTION) {       //满减
                     activityRule.set(this.computeFullReduction(totalAmount, activityRuleList));
                } else {       //满量
                    activityRule.set(this.computeFullDiscount(cartNum, totalAmount, activityRuleList));
                }

                //CartInfoVo
                CartInfoVo cartInfoVo = new CartInfoVo();
                cartInfoVo.setActivityRule(activityRule.get());
                cartInfoVo.setCartInfoList(infos);
                cartInfoVos.add(cartInfoVo);

                //记录哪些购物项参与活动

            });
        }

        //没有活动购物项skuId

        return cartInfoVos;
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

    private BigDecimal computeTotalAmount(List<CartInfo> cartInfoList) {
        BigDecimal total = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                BigDecimal itemTotal = cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    /**
     * 计算满量打折最优规则
     * @param totalNum
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠折扣从大到小排序了
     */
    private ActivityRule computeFullDiscount(Integer totalNum, BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项购买个数大于等于满减件数，则优化打折
            if (totalNum.intValue() >= activityRule.getConditionNum()) {
                BigDecimal skuDiscountTotalAmount = totalAmount.multiply(activityRule.getBenefitDiscount().divide(new BigDecimal("10")));
                BigDecimal reduceAmount = totalAmount.subtract(skuDiscountTotalAmount);
                activityRule.setReduceAmount(reduceAmount);
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，还差")
                    .append(totalNum-optimalActivityRule.getConditionNum())
                    .append("件");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionNum())
                    .append("元打")
                    .append(optimalActivityRule.getBenefitDiscount())
                    .append("折，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }

    /**
     * 计算满减最优规则
     * @param totalAmount
     * @param activityRuleList //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
     */
    private ActivityRule computeFullReduction(BigDecimal totalAmount, List<ActivityRule> activityRuleList) {
        ActivityRule optimalActivityRule = null;
        //该活动规则skuActivityRuleList数据，已经按照优惠金额从大到小排序了
        for (ActivityRule activityRule : activityRuleList) {
            //如果订单项金额大于等于满减金额，则优惠金额
            if (totalAmount.compareTo(activityRule.getConditionAmount()) > -1) {
                //优惠后减少金额
                activityRule.setReduceAmount(activityRule.getBenefitAmount());
                optimalActivityRule = activityRule;
                break;
            }
        }
        if(null == optimalActivityRule) {
            //如果没有满足条件的取最小满足条件的一项
            optimalActivityRule = activityRuleList.get(activityRuleList.size()-1);
            optimalActivityRule.setReduceAmount(new BigDecimal("0"));
            optimalActivityRule.setSelectType(1);

            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，还差")
                    .append(totalAmount.subtract(optimalActivityRule.getConditionAmount()))
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
        } else {
            StringBuffer ruleDesc = new StringBuffer()
                    .append("满")
                    .append(optimalActivityRule.getConditionAmount())
                    .append("元减")
                    .append(optimalActivityRule.getBenefitAmount())
                    .append("元，已减")
                    .append(optimalActivityRule.getReduceAmount())
                    .append("元");
            optimalActivityRule.setRuleDesc(ruleDesc.toString());
            optimalActivityRule.setSelectType(2);
        }
        return optimalActivityRule;
    }


    private int computeCartNum(List<CartInfo> cartInfoList) {
        int total = 0;
        for (CartInfo cartInfo : cartInfoList) {
            //是否选中
            if(cartInfo.getIsChecked().intValue() == 1) {
                total += cartInfo.getSkuNum();
            }
        }
        return total;
    }
}
