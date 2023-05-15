package com.oumuanode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oumuanode.domain.entity.RoleMenu;

public interface RoleMenuService extends IService<RoleMenu> {

    void deleteRoleMenuByRoleId(Long id);
}