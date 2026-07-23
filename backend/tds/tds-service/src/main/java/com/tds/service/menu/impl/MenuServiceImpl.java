package com.tds.service.menu.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbMenu;
import com.tds.dal.mapper.TbMenuMapper;
import com.tds.service.menu.IMenuService;
import com.tds.service.menu.MenuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
public class MenuServiceImpl implements IMenuService {

    @Autowired
    private TbMenuMapper menuMapper;

    @Override
    public List<TbMenu> getMenuTree() {
        LambdaQueryWrapper<TbMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMenu::getfStatus, 1)
               .eq(TbMenu::getfDeleteMark, 0)
               .orderByAsc(TbMenu::getfSortOrder);
        List<TbMenu> allMenus = menuMapper.selectList(wrapper);

        // 按 parentId 分组
        List<TbMenu> rootMenus = allMenus.stream()
                .filter(m -> m.getfParentId() == null || m.getfParentId().isEmpty())
                .collect(Collectors.toList());

        // 递归构建树
        for (TbMenu root : rootMenus) {
            buildChildren(root, allMenus);
        }

        return rootMenus;
    }

    private void buildChildren(TbMenu parent, List<TbMenu> allMenus) {
        List<TbMenu> children = allMenus.stream()
                .filter(m -> parent.getfId().equals(m.getfParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (TbMenu child : children) {
            buildChildren(child, allMenus);
        }
    }

    @Override
    public TbMenu getMenuById(String id) {
        TbMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return menu;
    }

    @Override
    @Transactional
    public TbMenu createMenu(MenuDTO dto) {
        TbMenu menu = new TbMenu();
        menu.setfId(UUID.randomUUID().toString().replace("-", ""));
        menu.setfParentId(dto.getParentId());
        menu.setfMenuName(dto.getMenuName());
        menu.setfMenuCode(dto.getMenuCode());
        menu.setfMenuType(dto.getMenuType() != null ? dto.getMenuType() : 1);
        menu.setfPath(dto.getPath());
        menu.setfIcon(dto.getIcon());
        menu.setfSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        menu.setfStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        menu.setfTenantId(dto.getTenantId());
        menu.setfDeleteMark(0);
        menu.setfCreateTime(LocalDateTime.now());
        menu.setfUpdateTime(LocalDateTime.now());

        menuMapper.insert(menu);
        return menu;
    }

    @Override
    @Transactional
    public TbMenu updateMenu(String id, MenuDTO dto) {
        TbMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        if (dto.getParentId() != null) {
            menu.setfParentId(dto.getParentId());
        }
        if (dto.getMenuName() != null) {
            menu.setfMenuName(dto.getMenuName());
        }
        if (dto.getMenuCode() != null) {
            menu.setfMenuCode(dto.getMenuCode());
        }
        if (dto.getMenuType() != null) {
            menu.setfMenuType(dto.getMenuType());
        }
        if (dto.getPath() != null) {
            menu.setfPath(dto.getPath());
        }
        if (dto.getIcon() != null) {
            menu.setfIcon(dto.getIcon());
        }
        if (dto.getSortOrder() != null) {
            menu.setfSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            menu.setfStatus(dto.getStatus());
        }
        menu.setfUpdateTime(LocalDateTime.now());

        menuMapper.updateById(menu);
        return menu;
    }

    @Override
    @Transactional
    public void deleteMenu(String id) {
        TbMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 检查是否有子菜单
        LambdaQueryWrapper<TbMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMenu::getfParentId, id);
        long childCount = menuMapper.selectCount(wrapper);
        if (childCount > 0) {
            throw new BusinessException("请先删除子菜单");
        }

        menu.setfDeleteMark(1);
        menu.setfUpdateTime(LocalDateTime.now());
        menuMapper.updateById(menu);
    }
}
