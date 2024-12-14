package com.atguigu.ssyx.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.ssyx.common.auth.AuthContextHolder;
import com.atguigu.ssyx.common.constant.RedisConst;
import com.atguigu.ssyx.common.exception.SsyxException;
import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.common.result.ResultCodeEnum;
import com.atguigu.ssyx.common.utils.JwtHelper;
import com.atguigu.ssyx.enums.UserType;
import com.atguigu.ssyx.model.user.User;
import com.atguigu.ssyx.user.service.UserService;
import com.atguigu.ssyx.user.utils.ConstantPropertiesUtil;
import com.atguigu.ssyx.user.utils.HttpClientUtils;
import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import com.atguigu.ssyx.vo.user.UserLoginVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/weixin")
public class WeixinApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    //用户微信授权登录
    @ApiOperation("微信登录获取openId(小程序)")
    @GetMapping("/wxLogin/{code}")
    public Result loginWx(@PathVariable String code){
        //1 根据code、appId和appSecret，使用HttpClient工具请求微信接口服务
        //就是前台传过来的参数
        //2 请求微信接口服务，返回两个值session_key 和 openId(openId是微信的唯一标识)
        String wxOpenAppId = ConstantPropertiesUtil.WX_OPEN_APP_ID;
        String wxOpenAppSecret = ConstantPropertiesUtil.WX_OPEN_APP_SECRET;
        //get请求
        StringBuffer url = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/jscode2session")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&js_code=%s")
                .append("&grant_type=authorization_code");
        String tokenUrl = String.format(url.toString(),
                wxOpenAppId,
                wxOpenAppSecret,
                code);
        //HttpClient发送get请求
        String result = null;
        try {
            result = HttpClientUtils.get(tokenUrl);
        } catch (Exception e) {
            throw new SsyxException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        String session_key = jsonObject.getString("session_key");
        String openId = jsonObject.getString("openId");
        //3 如果是第一次使用微信进行授权登陆
        //3.1 如果是第一次，添加微信的用户信息到数据库中
        User user =userService.getUserByOpenId(openId);
        if(ObjectUtils.isEmpty(user)){
             user = new User();
            user.setOpenId(openId);
            user.setNickName(openId);
            user.setPhotoUrl("");
            user.setUserType(UserType.USER);
            user.setIsNew(0);
            userService.save(user);
        }
        //3.2 如果不是第一次，

        //4 根据userId查询提货点（user_delivery表）和团长（leader表）信息
        LeaderAddressVo leaderAddressVo = userService.getLeaderAddressByUserId(user.getId());

        //5 使用JWT工具根据userId和userName生成token字符串
        String token = JwtHelper.createToken(user.getId(), user.getNickName());

        //6 获取当前登录用户信息，放到redis里面，设置有效时间
        UserLoginVo userLoginVo = userService.getUserLoginVo(user.getId());
        //param：key value 有效时间 时间单位
        redisTemplate.opsForValue()
                .set(RedisConst.USER_KEY_PREFIX + user.getId(),userLoginVo,RedisConst.USERKEY_TIMEOUT, TimeUnit.DAYS);

        //7 需要数据封装到map返回
        HashMap<String, Object> map = new HashMap<>();
        map.put("user",user);
        map.put("token",token);
        map.put("leaderAddressVo",leaderAddressVo);

        return Result.ok(map);
    }

    @PostMapping("/auth/updateUser")
    @ApiOperation(value = "更新用户昵称与头像")
    public Result updateUser(@RequestBody User user) {
        //获取当前登录用户id
        User user1 = userService.getById(AuthContextHolder.getUserId());
        //把昵称更新为微信用户
        //把表情替换成*
        user1.setNickName(user.getNickName().replaceAll("[ue000-uefff]","*"));
        user1.setPhotoUrl(user.getPhotoUrl());
        userService.updateById(user1);
        return Result.ok(null);
    }
}
