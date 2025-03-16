package com.atguigu.ssyx.cart.service.impl;

import com.atguigu.ssyx.cart.service.CartInfoService;
import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.common.constant.RedisConst;
import com.atguigu.ssyx.common.exception.SsyxException;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.enums.SkuType;
import com.atguigu.ssyx.model.order.CartInfo;
import com.atguigu.ssyx.vo.product.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;




    @Override
    public void addToCart(Long userId, Long skuId, Integer skuNum) {
        //1 因为购物车数据存储到redis里面，
        String cartKey = this.getCartKey(userId);
        //绑定一个指定的哈希键
        BoundHashOperations<String,String, CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);

        //2 根据第一步查询出来的结果，得到skuId + skuNum
        //进行判断 结果里面是否有skuId
        //目的：判断是否是第一次添加这个商品到购物车
        CartInfo cartInfo = null;
        if(hashOperations.hasKey(skuId.toString())){
            //3、如果结果里包含skuId,不是第一次添加
            //3.1、根据skuId,获取对应的数量，并更新
            cartInfo = hashOperations.get(skuId.toString());
            Integer currentSkuNum = cartInfo.getSkuNum() + skuNum;
            if(currentSkuNum < 1){
                return;
            }
            //更新cartInfo
            cartInfo.setSkuNum(currentSkuNum);
            cartInfo.setCurrentBuyNum(currentSkuNum);
            //判断商品数量不能大于限购数量
            Integer perLimit = cartInfo.getPerLimit();
            if(currentSkuNum > perLimit){
                throw new SsyxException(ResultCodeEnum.SKU_LIMIT_ERROR);
            }
            //更新其他值
            cartInfo.setIsChecked(1);
            cartInfo.setUpdateTime(new Date());
        } else {
            //4、如果结果里面没有skuId，是第一次添加
            //4.1 直接添加
            skuNum = 1;
            cartInfo = new CartInfo();
            //通过远程调用，根据skuId获取skuInfo
            SkuInfoVo skuInfo = productFeignClient.getSkuInfoVo(skuId);
            if(skuInfo == null){
                throw new SsyxException(ResultCodeEnum.DATA_ERROR);
            }
            cartInfo.setSkuId(skuId);
            cartInfo.setCategoryId(skuInfo.getCategoryId());
            cartInfo.setSkuType(skuInfo.getSkuType());
            cartInfo.setIsNewPerson(skuInfo.getIsNewPerson());
            cartInfo.setUserId(userId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCurrentBuyNum(skuNum);
            cartInfo.setSkuType(SkuType.COMMON.getCode());
            cartInfo.setPerLimit(skuInfo.getPerLimit());
            cartInfo.setImgUrl(skuInfo.getImgUrl());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setWareId(skuInfo.getWareId());
            cartInfo.setIsChecked(1);
            cartInfo.setStatus(1);
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }

        //5 更新redis缓存
        hashOperations.put(skuId.toString(),cartInfo);

        //6 设置过期时间
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void deleteCart(Long skuId, Long userId) {
        //绑定一个指定的哈希键
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(this.getCartKey(userId));

        //删除
        if(hashOperations.hasKey(skuId.toString())){
            hashOperations.delete(skuId.toString());
        }
    }

    @Override
    public void deleteAllCart(Long userId) {
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(this.getCartKey(userId));
        List<CartInfo> cartInfoList = hashOperations.values();

        for(CartInfo cartInfo :cartInfoList){
            hashOperations.delete(cartInfo.getSkuId().toString());
        }
    }

    @Override
    public void batchDeleteCart(Long userId, List<Long> skuIds) {
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(this.getCartKey(userId));
        List<CartInfo> cartInfoList = hashOperations.values();
        skuIds.forEach(skuId -> {
            hashOperations.delete(skuId.toString());
        });
    }

    @Override
    public List<CartInfo> cartList(Long userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();

        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> hashOperations = redisTemplate.boundHashOps(cartKey);
        cartInfoList = hashOperations.values();
        if(!CollectionUtils.isEmpty(cartInfoList)){
            cartInfoList.stream().sorted(Comparator.comparing(CartInfo::getCreateTime).reversed()).collect(Collectors.toList());
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(Long userId, Long skuId, Integer isChecked) {
        //获取到redis的key
        String cartKey = this.getCartKey(userId);

        //泛型是存储在redis中的hash类型 结构是 userId skuId cartInfo
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        CartInfo cartInfo = boundHashOperations.get(skuId.toString());
        if(cartInfo != null){
            cartInfo.setIsChecked(isChecked);
        }
        //更新
        boundHashOperations.put(skuId.toString(),cartInfo);
        //设置key的过期时间
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void checkAllCart(Long userId, Integer isChecked) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = boundHashOperations.values();
        Map<String, CartInfo> map = new HashMap<>();
        cartInfoList.forEach(cartInfo -> {
            cartInfo.setIsChecked(isChecked);
            map.put(cartInfo.getSkuId().toString(),cartInfo);
        });
        boundHashOperations.putAll(map);

        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void batchCheckCart(Long userId, List<Long> skuIdList, Integer isChecked) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        skuIdList.forEach(skuId -> {
            CartInfo cartInfo = boundHashOperations.get(skuId);
            cartInfo.setIsChecked(isChecked);
            boundHashOperations.put(cartInfo.getSkuId().toString(),cartInfo);
        });
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        List<CartInfo> cartInfoList = boundHashOperations.values();
        List<CartInfo> cartInfoListChecked = cartInfoList.stream().filter(cartInfo -> cartInfo.getIsChecked().intValue() == 1).collect(Collectors.toList());
        return cartInfoListChecked;
    }

    @Override
    public void deleteCartChecked(Long userId) {
        //根据userId查询选中购物车记录
        List<CartInfo> cartCheckedList = this.getCartCheckedList(userId);

        //得到skuId集合
        List<Long> skuIds = cartCheckedList.stream().map(item -> item.getSkuId()).collect(Collectors.toList());

        //构建redis的key值
        String cartKey = this.getCartKey(userId);
        BoundHashOperations<String,String,CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);

        //根据filed值删除redis数据
        skuIds.forEach(skuId -> {
            boundHashOps.delete(skuId.toString());
        });

    }

    //返回购物车在redis的key
    private String getCartKey(Long userId){
        //user:userId:cart
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }


    //设置key过期时间
    private void setCartKeyExpire(String key){
        redisTemplate.expire(key,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }
}
