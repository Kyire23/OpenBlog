package com.oumuanode.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oumuanode.domain.entity.UserRole;
import com.oumuanode.mapper.UserRoleMapper;
import com.oumuanode.service.UserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
}
