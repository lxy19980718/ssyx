package com.atguigu.ssyx.acl.utils;

import com.atguigu.ssyx.model.acl.Permission;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    public static List<Permission> buildPermission(List<Permission> allPermissionList) {
    List<Permission> trees = new ArrayList<>();
    //遍历所有菜单list集合，得到第一层数据，pid=0
    for (Permission permission:allPermissionList){
        if(permission.getPid() ==0){
            permission.setLevel(1);
            //调用方法，从第一层开始往下找
            trees.add(findChildren(permission,allPermissionList));
        }
    }
    return trees;

    }

    //递归往下去找，找子节点
    private static Permission findChildren(Permission permission, List<Permission> allPermissionList) {
        permission.setChildren(new ArrayList<Permission>());
        //遍历allList所有菜单数据
        //判断：当前节点id = pid是否一样，封装递归往下去找
        for(Permission it:allPermissionList){
            if(it.getPid()== permission.getId()){
                int Level = permission.getLevel()+1;
                it.setLevel(Level);
                if(permission.getChildren() == null){
                    permission.setChildren(new ArrayList<>());
                }
                //封装下一层数据
                permission.getChildren().add(findChildren(it,allPermissionList));

            }
        }
        return permission;
    }
}
