package com.atguigu.ssyx.client.user;

import com.atguigu.ssyx.vo.user.LeaderAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//指定接口所在的服务名字！！！
@FeignClient(value = "service-user")
public interface UserFeignClient {

    @GetMapping("/api/user/leader/inner/getUserAddressByUserId/{userId}")
    public LeaderAddressVo getUserAddressByUserId(@PathVariable Long userId);

}
