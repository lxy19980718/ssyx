package com.atguigu.ssyx.product.mapper;


import com.atguigu.ssyx.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * sku信息 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    //解锁库存
    void unlockStock(@Param("skuId") Long skuId,@Param("skuNum") Integer skuNum);

    //检查库存
    SkuInfo checkStock(@Param("skuId") Long skuId,@Param("skuNum") Integer skuNum);

    //锁定库存
    Integer lockStock(@Param("skuId") Long skuId,@Param("skuNum") Integer skuNum);
}
