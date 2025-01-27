package com.atguigu.ssyx.product.service;


import com.atguigu.ssyx.model.product.SkuPoster;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商品海报表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
public interface SkuPosterService extends IService<SkuPoster> {

    List<SkuPoster> getSkuPoster(Long id);
}
