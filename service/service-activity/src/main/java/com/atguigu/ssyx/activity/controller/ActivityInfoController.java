package com.atguigu.ssyx.activity.controller;


import com.atguigu.ssyx.activity.service.ActivityInfoService;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.activity.ActivityInfo;
import com.atguigu.ssyx.model.product.SkuInfo;
import com.atguigu.ssyx.vo.activity.ActivityRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2024-02-11
 */
@RestController
@Api(tags = "活动管理")
@RequestMapping("admin/activity/activityInfo")
public class ActivityInfoController {

    @Resource
    ActivityInfoService activityInfoService;

    @ApiOperation("分页查询")
    @GetMapping("{page}/{limit}")
    public Result getPageList(@PathVariable Long page,@PathVariable Long limit){
        Page<ActivityInfo> pageParam = new Page<>(page, limit);
        IPage<ActivityInfo> pageModel = activityInfoService.selectPageActivityInfo(pageParam);
        return Result.ok(pageModel);
    }

    @PostMapping("/save")
    @ApiOperation("添加活动")
    public Result save(@RequestBody ActivityInfo activityInfo){
        activityInfoService.save(activityInfo);
        return Result.ok(null);
    }

    @DeleteMapping("/remove/{id}")
    @ApiOperation("删除活动")
    public Result removeById(@PathVariable Long id){
        activityInfoService.removeById(id);
        return Result.ok(null);
    }

    @PutMapping("/update")
    @ApiOperation("修改活动")
    public Result updateById(ActivityInfo activityInfo){
        activityInfoService.updateById(activityInfo);
        return Result.ok(null);
    }

    //营销活动规则相关接口
    //1、根据活动id获取活动规则数据
    @GetMapping("findActivityRuleList/{id}")
    @ApiOperation("根据活动id获取活动规则数据")
    public Result findActivityRuleList(@PathVariable Long id){
        Map<String,Object> activityRuleMap = activityInfoService.findActivityRuleList(id);
        return Result.ok(activityRuleMap);
    }
    //2、在活动里面添加规则数据
    @PostMapping("saveActivityRule")
    @ApiOperation("在活动里面添加规则数据")
    public Result saveActivityRule(@RequestBody ActivityRuleVo activityRuleVo){
        activityInfoService.saveActivityRule(activityRuleVo);
        return Result.ok(null);
    }
    //3、根据关键字查询匹配得sku信息
    @GetMapping("findSkuInfoByKeyword/{keyword}")
    @ApiOperation("根据关键字查询匹配得sku信息")
    public Result findSkuInfoByKeyword(@PathVariable("keyword") String keyword){
        List<SkuInfo> skuInfoList = activityInfoService.findSkuInfoByKeyword(keyword);
        return Result.ok(skuInfoList);
    }

    @GetMapping("get/{id}")
  public Result get(@PathVariable("id") Long id){
        ActivityInfo activityInfo = activityInfoService.getById(id);
        activityInfo.setActivityTypeString(activityInfo.getActivityType().getComment());
        return Result.ok(activityInfo);
    }
}

