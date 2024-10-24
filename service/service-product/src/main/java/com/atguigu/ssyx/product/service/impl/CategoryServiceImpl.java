package com.atguigu.ssyx.product.service.impl;


import com.atguigu.ssyx.model.product.Category;
import com.atguigu.ssyx.product.mapper.CategoryMapper;
import com.atguigu.ssyx.product.service.CategoryService;
import com.atguigu.ssyx.vo.product.CategoryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 商品三级分类 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-11-25
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public IPage<Category> selectPageCategory(Page<Category> pageParam, CategoryVo categoryVo) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        String name = categoryVo.getName();
        if(!StringUtils.isEmpty(name)){
            queryWrapper.like(Category::getName,name);
        }
        IPage<Category> categoryPage = baseMapper.selectPage(pageParam, queryWrapper);
        return categoryPage;
    }

    @Override
    public List<Category> getByCategoryIds(List<Long> categoryIds) {
        return this.listByIds(categoryIds);
    }
}
