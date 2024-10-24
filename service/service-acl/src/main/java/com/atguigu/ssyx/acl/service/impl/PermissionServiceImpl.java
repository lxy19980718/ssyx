package com.atguigu.ssyx.acl.service.impl;

import com.atguigu.ssyx.acl.mapper.PermissionServiceMapper;
import com.atguigu.ssyx.acl.service.PermissionService;
import com.atguigu.ssyx.acl.service.RolePermissionService;
import com.atguigu.ssyx.model.acl.Permission;
import com.atguigu.ssyx.model.acl.RolePermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionServiceMapper, Permission> implements PermissionService {


    @Autowired
    private RolePermissionService rolePermissionService;
    @Override
    public List<Permission> queryAllPermission() {
        List<Permission> permissions = new ArrayList<>();

        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPid,0);

        permissions = baseMapper.selectList(wrapper);

        permissions.stream().forEach(item->{
           item.setLevel(1);
           this.getChildList(item.getId(),item,item.getLevel());
        });
        return  permissions ;
    }

    private void getChildList(Long id,Permission item,Integer level) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPid,id);

        List<Permission> childList = baseMapper.selectList(wrapper);
        //if(childList.size() != 0){
            item.setChildren(childList);
            childList.stream().forEach(e->{
                e.setLevel(level+1);
                this.getChildList(e.getId(),e,level+1);
            });
        //}
    }

    //@Override
    //public List<Permission> queryAllPermission() {
    //    //查询所有菜单
    //    List<Permission> allPermissionList = baseMapper.selectList(null);
    //
    //    //转换要求数据格式
    //    List<Permission> result = PermissionHelper.buildPermission(allPermissionList);
    //    return result;
    //}

    @Override
    public void removeChildById(Long id) {
        ArrayList<Long> idList = new ArrayList<>();

        idList.add(id);

        this.getAllPermissionId(id,idList);

    }

    @Override
    public List<Permission> toAssign(Long roleId) {
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper();
        wrapper.eq(RolePermission::getRoleId,roleId);
        List<RolePermission> list = rolePermissionService.list(wrapper);


        List<Long> idList = list.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<Permission> permissions = this.queryAllPermission();

        //递归 查看当前权限是否被分配
        this.checkPermission(permissions,idList);
        return permissions;
    }

    private void checkPermission(List<Permission> permissions, List<Long> idList) {
        permissions.stream().forEach(item->{
            if(idList.contains(item.getId())){
                item.setSelect(Boolean.TRUE);
                this.checkPermission(item.getChildren(),idList);
            }
        });
    }

    //递归找到当前菜单下面的所有子菜单
    private void getAllPermissionId(Long id, List<Long> idList) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPid,id);
        List<Permission> childList = baseMapper.selectList(wrapper);
        childList.stream().forEach(item->{
            idList.add(item.getId());
            this.getAllPermissionId(item.getId(),idList);
        });

        this.baseMapper.deleteBatchIds(idList);
    }
}
