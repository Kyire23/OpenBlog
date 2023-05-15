package com.oumuanode.service.impl;

import com.oumuanode.domain.ResponseResult;
import com.oumuanode.domain.entity.LoginUser;
import com.oumuanode.domain.entity.User;
import com.oumuanode.service.LoginService;
import com.oumuanode.utils.JwtUtil;
import com.oumuanode.utils.RedisCache;
import com.oumuanode.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

@Service
public class SystemLoginServiceImpl implements LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    @Override
    public ResponseResult login(User user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserName(),user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        if (Objects.isNull(authenticate)){
            throw new RuntimeException("用户名或密码错误");
        }

        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        //把用户信息存入redis
        redisCache.setCacheObject("login:"+userId,loginUser);

        HashMap<String, String> map = new HashMap<>();
        map.put("token",jwt);

        return ResponseResult.okResult(map);
    }

    @Override
    public ResponseResult logout() {
        Long userId = SecurityUtils.getUserId();

        redisCache.deleteObject("login:"+userId);
        return ResponseResult.okResult();

    }
}