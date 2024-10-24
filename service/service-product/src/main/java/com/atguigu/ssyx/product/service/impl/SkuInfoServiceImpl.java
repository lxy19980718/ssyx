package com.atguigu.ssyx.product.service.impl;


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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

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
}
