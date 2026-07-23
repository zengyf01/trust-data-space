package com.tds.api.menu;

import com.tds.common.core.ApiResponse;
import com.tds.dal.entity.TbMenu;
import com.tds.service.menu.IMenuService;
import com.tds.service.menu.MenuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private IMenuService menuService;

    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    public ApiResponse<?> getMenuTree() {
        List<TbMenu> tree = menuService.getMenuTree();
        return ApiResponse.success(tree);
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getMenuById(@PathVariable String id) {
        return ApiResponse.success(menuService.getMenuById(id));
    }

    /**
     * 创建菜单
     */
    @PostMapping
    public ApiResponse<?> createMenu(@RequestBody MenuDTO dto) {
        return ApiResponse.success(menuService.createMenu(dto));
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    public ApiResponse<?> updateMenu(@PathVariable String id, @RequestBody MenuDTO dto) {
        return ApiResponse.success(menuService.updateMenu(id, dto));
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteMenu(@PathVariable String id) {
        menuService.deleteMenu(id);
        return ApiResponse.success(null);
    }
}
