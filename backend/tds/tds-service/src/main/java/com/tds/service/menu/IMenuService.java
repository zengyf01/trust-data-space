package com.tds.service.menu;

import com.tds.dal.entity.TbMenu;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface IMenuService {

    /**
     * 获取菜单树
     */
    List<TbMenu> getMenuTree();

    /**
     * 获取菜单详情
     */
    TbMenu getMenuById(String id);

    /**
     * 创建菜单
     */
    TbMenu createMenu(MenuDTO dto);

    /**
     * 更新菜单
     */
    TbMenu updateMenu(String id, MenuDTO dto);

    /**
     * 删除菜单
     */
    void deleteMenu(String id);
}
