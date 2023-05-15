package com.oumuanode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oumuanode.domain.ResponseResult;
import com.oumuanode.domain.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {

    List<String> selectRoleKeyByUserId(Long id);

    ResponseResult selectRolePage(Role role, Integer pageNum, Integer pageSize);

    void insertRole(Role role);

    void updateRole(Role role);

    List<Role> selectRoleAll();

    List<Long> selectRoleIdByUserId(Long userId);
}