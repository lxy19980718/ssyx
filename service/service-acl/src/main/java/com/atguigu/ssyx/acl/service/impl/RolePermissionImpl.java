package com.atguigu.ssyx.acl.service.impl;


import com.atguigu.ssyx.acl.mapper.RolePermissionMapper;
import com.atguigu.ssyx.acl.service.RolePermissionService;
import com.atguigu.ssyx.model.acl.RolePermission;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
