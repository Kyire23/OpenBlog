package com.oumuanode.controller;

import com.oumuanode.domain.ResponseResult;
import com.oumuanode.domain.entity.Role;
import com.oumuanode.domain.entity.User;
import com.oumuanode.domain.vo.UserInfoAndRoleIdsVo;
import com.oumuanode.enums.AppHttpCodeEnum;
import com.oumuanode.exception.SystemException;
import com.oumuanode.service.RoleService;
import com.oumuanode.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public ResponseResult list(User user, Integer pageNum, Integer pageSize) {
        return userService.selectUserPage(user,pageNum,pageSize);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public ResponseResult add(@RequestBody User user)
    {
        if(!StringUtils.hasText(user.getUserName())){
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        if (!userService.checkUserNameUnique(user.getUserName())){
            throw new SystemException(AppHttpCodeEnum.USERNAME_EXIST);
        }
        if (!userService.checkPhoneUnique(user)){
            throw new SystemException(AppHttpCodeEnum.PHONENUMBER_EXIST);
        }
        if (!userService.checkEmailUnique(user)){
            throw new SystemException(AppHttpCodeEnum.EMAIL_EXIST);
        }
        return userService.addUser(user);
    }

    @GetMapping(value = { "/{userId}" })
    public ResponseResult getUserInfoAndRoleIds(@PathVariable(value = "userId") Long userId)
    {
        List<Role> roles = roleService.selectRoleAll();
        User user = userService.getById(userId);

        List<Long> roleIds = roleService.selectRoleIdByUserId(userId);
        //当前用户所具有的角色id列表
        UserInfoAndRoleIdsVo userInfoAndRoleIdsVo = new UserInfoAndRoleIdsVo(user, roles, roleIds);
        return ResponseResult.okResult(userInfoAndRoleIdsVo);
    }
    @PutMapping
    public ResponseResult edit(@RequestBody User user) {
        userService.updateUser(user);
        return ResponseResult.okResult();
    }
}