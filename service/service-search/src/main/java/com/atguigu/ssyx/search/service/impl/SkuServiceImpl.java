package com.atguigu.ssyx.search.service.impl;

import com.atguigu.ssyx.activity.client.ActivityFeignClient;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.enums.SkuType;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.search.SkuEs;
import com.atguigu.ssyx.search.repository.SkuRepository;
import com.atguigu.ssyx.search.service.SkuService;
import com.atguigu.ssyx.vo.search.SkuEsQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuRepository skuRepository;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ActivityFeignClient activityFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<SkuEs> findHotSkuList() {
        // 0代表第一页
        Pageable pageable = PageRequest.of(0,3);

        //find read get 开头
        //关联条件关键字
        Page<SkuEs> page = skuRepository.findByOrderByHotScoreDesc(pageable);
        return page.getContent();
    }

    @Override
    public Page<SkuEs> search(Pageable pageable, SkuEsQueryVo skuEsQueryVo) {
        //1、向skuEsQueryVo设置wareId，当前登录用户的仓库Id
        skuEsQueryVo.setWareId(AuthContextHolder.getWareId());

        //2、调用skuRepository方法，根据SpringData命名规范定义方法
        Page<SkuEs> pageModel = null;
        String keyword = skuEsQueryVo.getKeyword();
        if(StringUtils.isEmpty(keyword)){
            //根据明明顺序传参数
            pageModel = skuRepository.findByCategoryIdAndWareId(skuEsQueryVo.getCategoryId(),skuEsQueryVo.getWareId(),pageable);
        } else {
            pageModel = skuRepository.findByWareIdAndkeyWord(skuEsQueryVo.getWareId(),skuEsQueryVo.getKeyword(),pageable);
        }

        //3、查询商品参加的优惠活动
        List<SkuEs> skuEsList = pageModel.getContent();
        if(!CollectionUtils.isEmpty(skuEsList)){
            //遍历skuEsList，得到所有skuId
            List<Long> ids = skuEsList.stream().map(item -> item.getId()).collect(Collectors.toList());
            //根据ids进行远程调用，调用service-activity里面的接口，得到数据
            //返回Map<skuId,参与活动的规则名称的列表>
            Map<Long, List<String>> skuIdAndRuleMap = activityFeignClient.findActivity(ids);

            //封装
            if(skuIdAndRuleMap != null){
                skuEsList.forEach(skuEs -> skuEs.setRuleList(skuIdAndRuleMap.get(skuEs.getId())));
            }
        }
        return pageModel;
    }


    //更新商品热度
    @Override
    public Boolean incrHotScore(Long skuId) {
        String key = "hotScore";
        //redis保存数据，每次加1
        Double hotScore = redisTemplate.opsForZSet().incrementScore(key,"skuId"+skuId,1);

        //规则
        if(hotScore%10 == 0){
            //更新es
            Optional<SkuEs> optional = skuRepository.findById(skuId);
            SkuEs skuEs = optional.get();
            skuEs.setHotScore(Math.round(hotScore));
            //有id做更新，没有id做添加
            skuRepository.save(skuEs);
        }

        return Boolean.TRUE;
    }


    @Override
    public void lowerSku(Long skuId) {
        skuRepository.deleteById(skuId);
    }


    @Override
    public void upperSku(Long skuId) {
        //1 通过远程调用，根据skuId获取相关信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if(skuInfo == null){
            return;
        }
        Category category = productFeignClient.getCategory(skuInfo.getCategoryId());
        //2 获取数据封装SkuEs对象
        SkuEs skuEs = new SkuEs();
       if(category != null){
           skuEs.setCategoryId(category.getId());
           skuEs.setCategoryName(category.getName());
       }
        skuEs.setId(skuInfo.getId());
        skuEs.setKeyword(skuInfo.getSkuName()+","+skuEs.getCategoryName());
        skuEs.setWareId(skuInfo.getWareId());
        skuEs.setIsNewPerson(skuInfo.getIsNewPerson());
        skuEs.setImgUrl(skuInfo.getImgUrl());
        skuEs.setTitle(skuInfo.getSkuName());

        if(skuInfo.getSkuType().equals(SkuType.COMMON.getCode())){//普通商品
            skuEs.setSkuType(0);
            skuEs.setPrice(skuInfo.getPrice().doubleValue());
        }

        //3 调用方法添加ES
        skuRepository.save(skuEs);
    }
}
