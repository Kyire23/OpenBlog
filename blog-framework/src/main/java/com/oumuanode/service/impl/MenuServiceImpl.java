package com.oumuanode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oumuanode.constants.SystemConstants;
import com.oumuanode.domain.entity.Menu;
import com.oumuanode.mapper.MenuMapper;
import com.oumuanode.service.MenuService;
import com.oumuanode.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.sun.javafx.robot.impl.FXRobotHelper.getChildren;

@Service("menuService")
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<String> selectPermsByUserId(Long id) {
        if (id == 1L){
            LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Menu::getMenuType, SystemConstants.MENU,SystemConstants.BUTTON);

            wrapper.eq(Menu :: getStatus,SystemConstants.STATUS_NORMAL);

            List<Menu> menus = list(wrapper);
            List<String> collect = menus.stream().map(Menu::getPerms).collect(Collectors.toList());

            return collect;

        }
        return getBaseMapper().selectPermsByUserId(id);
    }

    @Override
    public List<Menu> selectRouterMenuTreeByUserId(Long userId) {
        MenuMapper menuMapper = getBaseMapper();
        List<Menu> menus = null;
        if (SecurityUtils.isAdmin()){
            menus = menuMapper.selectAllRouterMenu();
        }else {
            menus = menuMapper.selectRouterMenuTreeByUserId(userId);
        }
        List<Menu> menusTree = createMenuTree(menus,0L);
        return menusTree;
    }

    private List<Menu> createMenuTree(List<Menu> menus, long parentId) {
        List<Menu> menuTree = menus.stream().filter(menu -> menu.getParentId().equals(parentId))
                .map(menu -> menu.setChildren(getChildren(menu, menus)))
                .collect(Collectors.toList());
        return menuTree;
        
    }
    /**
     * 获取存入参数的 子Menu集合
     * @param menu
     * @param menus
     * @return
     */
    private List<Menu> getChildren(Menu menu, List<Menu> menus) {
        List<Menu> childrenList = menus.stream().filter(menu1 -> menu1.getParentId().equals(menu.getId()))
                .map(menu1 -> menu1.setChildren(getChildren(menu1, menus)))
                .collect(Collectors.toList());
        return childrenList;
    }

    @Override
    public List<Menu> selectMenuList(Menu menu) {

        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        //menuName模糊查询
        queryWrapper.like(StringUtils.hasText(menu.getMenuName()),Menu::getMenuName,menu.getMenuName());
        queryWrapper.eq(StringUtils.hasText(menu.getStatus()),Menu::getStatus,menu.getStatus());
        //排序 parent_id和order_num
        queryWrapper.orderByAsc(Menu::getParentId,Menu::getOrderNum);
        List<Menu> menus = list(queryWrapper);;
        return menus;
    }

    @Override
    public boolean hasChild(Long menuId) {
        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Menu::getParentId,menuId);
        return count(queryWrapper) != 0;
    }

    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        return getBaseMapper().selectMenuListByRoleId(roleId);
    }
}