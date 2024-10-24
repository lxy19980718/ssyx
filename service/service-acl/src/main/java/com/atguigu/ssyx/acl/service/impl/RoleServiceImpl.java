package com.atguigu.ssyx.acl.service.impl;

import com.atguigu.ssyx.acl.mapper.RoleMapper;
import com.atguigu.ssyx.acl.service.AdminRoleService;
import com.atguigu.ssyx.acl.service.PermissionService;
import com.atguigu.ssyx.acl.service.RoleService;
import com.atguigu.ssyx.model.acl.AdminRole;
import com.atguigu.ssyx.model.acl.Role;
import com.atguigu.ssyx.vo.acl.RoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private AdminRoleService adminRoleService;


    @Override
    public IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo) {
        //获取条件值
        String roleName = roleQueryVo.getRoleName();

        //创建mp条件的对象
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        //判断条件值是否为空，不为空 封装查询条件
        if(!StringUtils.isEmpty(roleName)){
            wrapper.like(Role::getRoleName,roleName);
        }

        //调用方法 实现条件查询分页
        Page<Role> rolePage = baseMapper.selectPage(pageParam, wrapper);
        //返回分页对象
        return rolePage;
    }

    @Override
    public Map<String, Object> getRoleByAdminId(Long adminId) {
        //查询所有角色
        List<Role> allRolesList = baseMapper.selectList(null);

        //已经分配的角色
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminRole::getAdminId,adminId);
        List<AdminRole> adminRoleList = adminRoleService.list(wrapper);

        List<Long> roleIdsList = adminRoleList.stream().map(item -> item.getRoleId()).collect(Collectors.toList());
        List<Role> assignRoles= allRolesList.stream().filter(item -> roleIdsList.contains(item.getId())).collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>();
        map.put("allRolesList",allRolesList);
        map.put("assignRoles",assignRoles);

        return map;
    }

    @Override
    public void saveAdminRole(Long adminId, List<Long> roleId) {
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminRole::getAdminId,adminId);
        adminRoleService.remove(wrapper);
        List<AdminRole> list = new ArrayList<>();
         roleId.stream().forEach(item -> {
            AdminRole adminRole = new AdminRole();
            adminRole.setRoleId(item);
            adminRole.setAdminId(adminId);
             list.add(adminRole);
        });
         adminRoleService.saveBatch(list);
    }
}
