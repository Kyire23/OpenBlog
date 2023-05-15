package com.oumuanode.service;

import com.oumuanode.domain.ResponseResult;
import com.oumuanode.domain.entity.User;

public interface LoginService {
    ResponseResult login(User user);

    ResponseResult logout();
}