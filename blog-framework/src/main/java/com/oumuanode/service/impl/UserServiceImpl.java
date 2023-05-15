package com.oumuanode.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oumuanode.domain.ResponseResult;
import com.oumuanode.domain.entity.User;
import com.oumuanode.domain.entity.UserRole;
import com.oumuanode.domain.vo.PageVo;
import com.oumuanode.domain.vo.UserInfoVo;
import com.oumuanode.domain.vo.UserVo;
import com.oumuanode.enums.AppHttpCodeEnum;
import com.oumuanode.exception.SystemException;
import com.oumuanode.mapper.UserMapper;
import com.oumuanode.service.UserRoleService;
import com.oumuanode.service.UserService;
import com.oumuanode.utils.BeanCopyUtils;
import com.oumuanode.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户表(User)表服务实现类
 *
 * @author makejava
 * @since 2022-02-09 00:28:30
 */
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseResult userInfo() {
        Long userId = SecurityUtils.getUserId();
        User id = getById(userId);

        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(id, UserInfoVo.class);
        return ResponseResult.okResult(userInfoVo);
    }

    @Override
    public ResponseResult updateUserInfo(User user) {
        //TODO 小bug 已经解决
        updateById(user);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult register(User user) {
        if (!StringUtils.hasText(user.getUserName())) {
            throw new SystemException(AppHttpCodeEnum.USERNAME_NOT_NULL);
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new SystemException(AppHttpCodeEnum.PASSWORD_NOT_NULL);
        }

        if (!StringUtils.hasText(user.getEmail())) {
            throw new SystemException(AppHttpCodeEnum.EMAIL_NOT_NULL);
        }

        if (!StringUtils.hasText(user.getNickName())) {
            throw new SystemException(AppHttpCodeEnum.NICKNAME_NOT_NULL);
        }

        //对数据进行是否存在的判断

        if (userNameExist(user.getUserName())) {
            throw new SystemException(AppHttpCodeEnum.USERNAME_EXIST);
        }
        if (nickNameExist(user.getNickName())) {
            throw new SystemException(AppHttpCodeEnum.NICKNAME_EXIST);
        }

        String encode = passwordEncoder.encode(user.getPassword());
        user.setPassword(encode);
        save(user);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult selectUserPage(User user, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.hasText(user.getUserName()),User :: getUserName,user.getUserName());
        queryWrapper.like(StringUtils.hasText(user.getStatus()),User :: getStatus,user.getStatus());
        queryWrapper.like(StringUtils.hasText(user.getPhonenumber()),User :: getPhonenumber,user.getPhonenumber());

        Page<User> page = new Page<>();
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        page(page,queryWrapper);

        List<User> users = page.getRecords();

        List<UserVo> collect = users.stream().map(u -> BeanCopyUtils.copyBean(u, UserVo.class))
                .collect(Collectors.toList());

        PageVo pageVo = new PageVo();
        pageVo.setTotal(page.getTotal());
        pageVo.setRows(collect);

        return ResponseResult.okResult(pageVo);
    }
    @Override
    public boolean checkUserNameUnique(String userName) {
        return count(Wrappers.<User>lambdaQuery().eq(User::getUserName,userName))==0;
    }

    @Override
    public boolean checkPhoneUnique(User user) {
        return count(Wrappers.<User>lambdaQuery().eq(User::getPhonenumber,user.getPhonenumber()))==0;
    }


    @Override
    public boolean checkEmailUnique(User user) {
        return count(Wrappers.<User>lambdaQuery().eq(User::getEmail,user.getEmail()))==0;
    }

    @Override
    public ResponseResult addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        save(user);

        if (user.getRoleIds() != null && user.getRoleIds().length > 0){
            insertUserRole(user);
        }
        return ResponseResult.okResult();
    }

    @Autowired
    private UserRoleService userRoleService;

    private void insertUserRole(User user) {
        List<UserRole> collect = Arrays.stream(user.getRoleIds()).map(roleId -> new UserRole(user.getId(), roleId))
                .collect(Collectors.toList());

        userRoleService.saveBatch(collect);
    }

    @Override
    public void updateUser(User user) {
        // 删除用户与角色关联
        LambdaQueryWrapper<UserRole> userRoleUpdateWrapper = new LambdaQueryWrapper<>();
        userRoleUpdateWrapper.eq(UserRole :: getUserId,user.getId());
        // 新增用户与角色管理
        insertUserRole(user);
        // 更新用户信息
        updateById(user);
    }

    private boolean nickNameExist(String nickName) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getNickName, nickName);
        return this.count(queryWrapper) > 0;
    }


    private boolean userNameExist(String userName) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User :: getUserName,userName);
        return this.count(queryWrapper) > 0;
    }

}