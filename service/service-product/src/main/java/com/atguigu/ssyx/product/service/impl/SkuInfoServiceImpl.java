package com.atguigu.ssyx.product.service.impl;


import com.atguigu.ssyx.common.constant.RedisConst;
import com.atguigu.ssyx.common.exception.SsyxException;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.model.product.SkuAttrValue;
import com.atguigu.ssyx.model.product.SkuImage;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.product.SkuPoster;
import com.atguigu.ssyx.mq.constant.MqConst;
import com.atguigu.ssyx.mq.service.RabbitService;
import com.atguigu.ssyx.product.mapper.SkuInfoMapper;
import com.atguigu.ssyx.product.service.SkuAttrValueService;
import com.atguigu.ssyx.product.service.SkuImageService;
import com.atguigu.ssyx.product.service.SkuInfoService;
import com.atguigu.ssyx.product.service.SkuPosterService;
import com.atguigu.ssyx.vo.product.SkuInfoQueryVo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import com.atguigu.ssyx.vo.product.SkuStockLockVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * sku信息 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {

    @Autowired
    SkuImageService skuImageService;

    @Autowired
    SkuAttrValueService skuAttrValueService;

    @Autowired
    SkuPosterService skuPosterService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<SkuInfo> findNewPersonSkuInfoList() {
        //条件1：is_new_person为1
        //条件2：publish_status为1 上架
        //条件3：只选其中3个

        //第一种方式
        //return this.list(Wrappers.<SkuInfo>lambdaQuery()
        //                .eq(SkuInfo::getIsNewPerson,1)
        //                .eq(SkuInfo::getPublishStatus,1)
        //                .orderByDesc(SkuInfo::getStock))
        //        .stream().limit(3).collect(Collectors.toList());

        return this.list(Wrappers.<SkuInfo>lambdaQuery()
                        .eq(SkuInfo::getIsNewPerson,1)
                        .eq(SkuInfo::getPublishStatus,1)
                        .orderByDesc(SkuInfo::getStock)
                        .last("limit 3")).stream().collect(Collectors.toList());
    }

    @Override
    public IPage<SkuInfo> seletPageSkuInfo(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo) {

        String keyword = skuInfoQueryVo.getKeyword();
        String skuType = skuInfoQueryVo.getSkuType();
        Long categoryId = skuInfoQueryVo.getCategoryId();

        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)){
            wrapper.like(SkuInfo::getSkuName,keyword);
        }
        if(!StringUtils.isEmpty(skuType)){
            wrapper.like(SkuInfo::getSkuType,skuType);
        }
        if(!StringUtils.isEmpty(categoryId)){
            wrapper.like(SkuInfo::getCategoryId,categoryId);
        }
        return baseMapper.selectPage(pageParam,wrapper);
    }

    @Override
    public void saveSkuInfo(SkuInfoVo skuInfoVo) {
        //1、添加sku的基本信息
        //skuInfoVo--->skuInfo
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,skuInfo);
        baseMapper.insert(skuInfo);

        //2、保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)){
            skuPosterList.stream().forEach(e -> e.setSkuId(skuInfo.getId()));
            skuPosterService.saveBatch(skuPosterList);
        }

        //3、保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)){
            skuImagesList.stream().forEach(e -> e.setSkuId(skuInfo.getId()));
            skuImageService.saveBatch(skuImagesList);
        }
        //4、保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            skuAttrValueList.forEach(e -> e.setSkuId(skuInfo.getId()));
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    @Override
    public SkuInfoVo getSkuInfo(Long id) {

        //根据id查询sku基本信息
        SkuInfo skuInfo = this.getById(id);
        List<SkuPoster> skuPosters = skuPosterService.getSkuPoster(id);
        List<SkuImage> skuImages= skuImageService.getSkuImage(id);
        List<SkuAttrValue> skuAttrValues = skuAttrValueService.getSkuAttrValue(id);

        SkuInfoVo skuInfoVo = new SkuInfoVo();
        BeanUtils.copyProperties(skuInfo,skuInfoVo);
        skuInfoVo.setSkuPosterList(skuPosters);
        skuInfoVo.setSkuImagesList(skuImages);
        skuInfoVo.setSkuAttrValueList(skuAttrValues);
        return skuInfoVo;
    }

    @Override
    public void updateSkuInfoById(SkuInfoVo skuInfoVo) {
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,skuInfo);
        updateById(skuInfo);
        skuPosterService.remove(Wrappers.<SkuPoster>lambdaQuery().eq(SkuPoster::getSkuId,skuInfoVo.getId()));

        //保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)){
            skuPosterList.stream().forEach(e -> e.setSkuId(skuInfo.getId()));
            skuPosterService.saveBatch(skuPosterList);
        }

        //保存商品图片
        skuImageService.remove(Wrappers.<SkuImage>lambdaQuery().eq(SkuImage::getSkuId,skuInfoVo.getId()));
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)){
            skuImagesList.stream().forEach(e -> e.setSkuId(skuInfo.getId()));
            skuImageService.saveBatch(skuImagesList);
        }

        skuAttrValueService.remove(Wrappers.<SkuAttrValue>lambdaQuery().eq(SkuAttrValue::getSkuId,skuInfoVo.getId()));
        //4、保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            skuAttrValueList.forEach(e -> e.setSkuId(skuInfo.getId()));
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    @Override
    public void check(Long skuId, Integer status) {
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        skuInfo.setCheckStatus(status);
        this.updateById(skuInfo);
    }

    @Override
    public void publish(Long id, Integer status) {
        if(status == 1){    //上架
            SkuInfo skuInfo = baseMapper.selectById(id);
            skuInfo.setPublishStatus(status);
            this.updateById(skuInfo);
            //整合mq把数据同步到es里面
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,MqConst.ROUTING_GOODS_UPPER,id);
        } else {    //下架
            SkuInfo skuInfo = baseMapper.selectById(id);
            skuInfo.setPublishStatus(status);
            this.updateById(skuInfo);
            //整合mq把数据同步到es里面
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT,MqConst.ROUTING_GOODS_LOWER,id);
        }

    }

    @Override
    public void isNewPerson(Long skuId, Integer status) {
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        skuInfo.setIsNewPerson(status);
        this.updateById(skuInfo);
    }

    @Override
    public List<SkuInfo> getBySkuIds(List<Long> skuIds) {
        return this.list(Wrappers.<SkuInfo>lambdaQuery().in(SkuInfo::getId,skuIds));
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        return baseMapper.selectList(Wrappers.<SkuInfo>lambdaQuery().like(SkuInfo::getSkuName,keyword));
    }

    @Override
    public SkuInfoVo getSkuInfoVo(Long skuId) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();
        // skuId查询skuInfo
        SkuInfo skuInfo = baseMapper.selectById(skuId);

        //skuId查询sku图片
        List<SkuImage> imageList = skuImageService.getImageListBySkuId(skuId);

        //skuId查询sku宣传海报
        List<SkuPoster> skuPosterList = skuPosterService.getSkuPoster(skuId);


        //skuId查询sku属性
        List<SkuAttrValue> skuAttrValueList = skuAttrValueService.getSkuAttrValue(skuId);

        BeanUtils.copyProperties(skuInfo,skuInfoVo);
        skuInfoVo.setSkuImagesList(imageList);
        skuInfoVo.setSkuPosterList(skuPosterList);
        skuInfoVo.setSkuAttrValueList(skuAttrValueList);

        return skuInfoVo;
    }

    @Override
    public Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderNo) {
        //1 skuStockLockVoList 是要锁定的库存集合
        if(CollectionUtils.isEmpty(skuStockLockVoList)){
            throw new SsyxException(ResultCodeEnum.DATA_ERROR);
        }
        skuStockLockVoList.forEach(skuStockLockVo -> {
            this.checkLock(skuStockLockVo);
        });

        //只要有一个商品锁定失败，所有锁定成功的商品都要解锁
        boolean flag = skuStockLockVoList.stream().anyMatch(skuStockLockVo -> !skuStockLockVo.getIsLock());
        if(flag){
            //所有锁定成功的商品都解锁
            skuStockLockVoList.stream().filter(SkuStockLockVo::getIsLock).forEach(skuStockLockVo -> {
                baseMapper.unlockStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());
            });
            //返回失败状态
            return Boolean.FALSE;
        }

        //如果所有商品都锁定成功，redis中缓存相关数据，为了方便后面解锁和减库存
        redisTemplate.opsForValue().set(RedisConst.STOCK_INFO + orderNo,skuStockLockVoList);
        return Boolean.TRUE;
    }

    //遍历skuStockLockVoList得到每个商品，验证库存并锁定库存，具备原子性
    private void checkLock(SkuStockLockVo skuStockLockVo) {

        //获取锁 公平锁 在队列中等待时间最长的 优先得到锁
        //公平锁，就是保证客户端获取锁的顺序，跟他们请求获取锁的顺序，是一样的。
        // 公平锁需要排队，谁先申请获取这把锁，
        // 谁就可以先获取到这把锁，是按照请求的先后顺序来的。
        // RedisConst.SKUKEY_PREFIX + skuStockLockVo.getSkuId() 是锁的键名
        RLock fairLock = this.redissonClient.getFairLock(RedisConst.SKUKEY_PREFIX + skuStockLockVo.getSkuId());
        fairLock.lock();

        try {
            //验库存
            SkuInfo skuInfo = baseMapper.checkStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());
            if(skuInfo == null) {
                //库存中没有商品
                skuStockLockVo.setIsLock(false);
                return;
            }

            //有满足条件的商品，锁定库存
            Integer rows = baseMapper.lockStock(skuStockLockVo.getSkuId(),skuStockLockVo.getSkuNum());
            if(rows == 1){
                //锁定成功
                skuStockLockVo.setIsLock(true);
            }
        } finally {
            //解锁
            fairLock.unlock();
        }

    }
}
