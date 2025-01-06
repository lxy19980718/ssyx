package com.atguigu.ssyx.home.service.impl;

import com.atguigu.ssyx.client.product.ProductFeignClient;
import com.atguigu.ssyx.client.search.SearchFeignClient;
import com.atguigu.ssyx.client.user.UserFeignClient;
import com.atguigu.ssyx.home.service.HomeService;
import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.model.search.SkuEs;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private SearchFeignClient searchFeignClient;

    @Override
    public Map<String, Object> homeData(Long userId) {
        HashMap<String, Object> map = new HashMap<>();
        //1、根据userId获取当前登录用户的提货地址信息
        //远程调用service-user模块接口获取需要的数据
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        map.put("leaderAddressVo",leaderAddressVo);
        //2、获取所有分类信息
        //远程调用service-product模块接口获取需要的数据
        List<Category> categoryList = productFeignClient.findAllCategoryList();
        map.put("categoryList",categoryList);
        //3、获取新人专享的商品
        //远程调用service-product模块接口获取需要的数据
        List<SkuInfo> newPersonSkuInfoList = productFeignClient.findNewPersonSkuInfoList();
        map.put("newPersonSkuInfoList",newPersonSkuInfoList);
        //4、获取热销好货
        //远程调用service-search模块接口获取需要的数据
        //根据score评分排序
        List<SkuEs> hotSkuList = searchFeignClient.findHotSkuList();
        map.put("hotSkuList",hotSkuList);
        //5、封装获取的数据到map集合，返回

        return map;
    }
}
