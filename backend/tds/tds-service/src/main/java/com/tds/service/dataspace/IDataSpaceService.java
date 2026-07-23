package com.tds.service.dataspace;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.common.core.PageResult;
import com.tds.dal.entity.TbDataSpace;
import com.tds.dal.entity.TbDataSpaceMember;
import com.tds.dal.entity.TbDataSpaceResource;

import java.util.List;
import java.util.Map;

/**
 * 数据空间服务接口
 */
public interface IDataSpaceService {

    // ==================== 数据空间管理 ====================

    /**
     * 分页查询数据空间
     */
    PageResult<TbDataSpace> getSpacePage(int currentPage, int pageSize, String spaceName, String ownerId, Integer status);

    /**
     * 获取数据空间详情
     */
    TbDataSpace getSpaceById(String id);

    /**
     * 获取数据空间详情（通过编码）
     */
    TbDataSpace getSpaceByCode(String spaceCode);

    /**
     * 创建数据空间
     */
    TbDataSpace createSpace(DataSpaceDTO dto);

    /**
     * 更新数据空间
     */
    TbDataSpace updateSpace(String id, DataSpaceDTO dto);

    /**
     * 删除数据空间
     */
    void deleteSpace(String id);

    /**
     * 审核数据空间
     */
    TbDataSpace approveSpace(String id, Integer status);

    /**
     * 冻结/解冻数据空间
     */
    TbDataSpace freezeSpace(String id, Integer status);

    /**
     * 获取用户参与的数据空间列表
     */
    List<TbDataSpace> getUserSpaces(String userId);

    // ==================== 成员管理 ====================

    /**
     * 分页查询成员列表
     */
    PageResult<TbDataSpaceMember> getMemberPage(int currentPage, int pageSize, String spaceId, Integer role, Integer status);

    /**
     * 获取成员详情
     */
    TbDataSpaceMember getMemberById(String id);

    /**
     * 添加成员
     */
    TbDataSpaceMember addMember(DataSpaceMemberDTO dto);

    /**
     * 更新成员角色
     */
    TbDataSpaceMember updateMemberRole(String id, Integer role);

    /**
     * 审核成员加入申请
     */
    TbDataSpaceMember approveMember(String id, Integer status);

    /**
     * 移除成员
     */
    void removeMember(String id);

    /**
     * 获取用户在空间中的角色
     */
    TbDataSpaceMember getMemberRole(String spaceId, String userId);

    // ==================== 资源管理 ====================

    /**
     * 分页查询空间资源
     */
    PageResult<TbDataSpaceResource> getResourcePage(int currentPage, int pageSize, String spaceId, String resourceType);

    /**
     * 添加资源到空间
     */
    TbDataSpaceResource addResource(DataSpaceResourceDTO dto);

    /**
     * 更新资源访问级别
     */
    TbDataSpaceResource updateResourceAccess(String id, Integer accessLevel);

    /**
     * 从空间移除资源
     */
    void removeResource(String id);

    /**
     * 获取用户可访问的资源
     */
    List<TbDataSpaceResource> getUserAccessibleResources(String userId, String resourceType);
}