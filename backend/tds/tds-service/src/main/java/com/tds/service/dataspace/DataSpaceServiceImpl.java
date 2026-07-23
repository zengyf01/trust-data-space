package com.tds.service.dataspace;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.core.PageResult;
import com.tds.common.enums.DataSpaceStatus;
import com.tds.common.enums.MemberRole;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbDataSpace;
import com.tds.dal.entity.TbDataSpaceMember;
import com.tds.dal.entity.TbDataSpaceResource;
import com.tds.dal.mapper.TbDataSpaceMapper;
import com.tds.dal.mapper.TbDataSpaceMemberMapper;
import com.tds.dal.mapper.TbDataSpaceResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 数据空间服务实现
 */
@Service
public class DataSpaceServiceImpl implements IDataSpaceService {

    @Autowired
    private TbDataSpaceMapper dataSpaceMapper;

    @Autowired
    private TbDataSpaceMemberMapper memberMapper;

    @Autowired
    private TbDataSpaceResourceMapper resourceMapper;

    // ==================== 数据空间管理 ====================

    @Override
    public PageResult<TbDataSpace> getSpacePage(int currentPage, int pageSize,
            String spaceName, String ownerId, Integer status) {
        Page<TbDataSpace> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDataSpace> wrapper = new LambdaQueryWrapper<>();
        if (spaceName != null && !spaceName.isEmpty()) {
            wrapper.like(TbDataSpace::getfSpaceName, spaceName);
        }
        if (ownerId != null && !ownerId.isEmpty()) {
            wrapper.eq(TbDataSpace::getfOwnerId, ownerId);
        }
        if (status != null) {
            wrapper.eq(TbDataSpace::getfStatus, status);
        }
        wrapper.orderByDesc(TbDataSpace::getfCreateTime);
        long total = dataSpaceMapper.selectCount(wrapper);
        IPage<TbDataSpace> result = dataSpaceMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), total, currentPage, pageSize);
    }

    @Override
    public TbDataSpace getSpaceById(String id) {
        return dataSpaceMapper.selectById(id);
    }

    @Override
    public TbDataSpace getSpaceByCode(String spaceCode) {
        LambdaQueryWrapper<TbDataSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDataSpace::getfSpaceCode, spaceCode);
        return dataSpaceMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public TbDataSpace createSpace(DataSpaceDTO dto) {
        TbDataSpace space = new TbDataSpace();
        space.setfId(UUID.randomUUID().toString().replace("-", ""));
        space.setfSpaceCode("DS" + System.currentTimeMillis());
        space.setfSpaceName(dto.getSpaceName());
        space.setfSpaceDesc(dto.getSpaceDesc());
        // 如果前端没有传ownerId则自动生成
        String ownerId = dto.getOwnerId();
        if (ownerId == null || ownerId.isEmpty()) {
            ownerId = UUID.randomUUID().toString().replace("-", "");
        }
        space.setfOwnerId(ownerId);
        space.setfOwnerName(dto.getOwnerName() != null ? dto.getOwnerName() : "未知");
        // 如果前端没有传organizationId则自动生成
        String orgId = dto.getOrganizationId();
        if (orgId == null || orgId.isEmpty()) {
            orgId = UUID.randomUUID().toString().replace("-", "");
        }
        space.setfOrganizationId(orgId);
        space.setfOrganizationName(dto.getOrganizationName() != null ? dto.getOrganizationName() : "未知");
        space.setfStatus(DataSpaceStatus.ACTIVE.getCode());
        space.setfSpaceType(dto.getSpaceType() != null ? dto.getSpaceType() : "PRIVATE");
        space.setfMemberCount(1);
        space.setfResourceCount(0);
        space.setfTenantId(dto.getTenantId());
        space.setfCreateTime(LocalDateTime.now());
        space.setfUpdateTime(LocalDateTime.now());
        space.setfDeleteMark(0);

        dataSpaceMapper.insert(space);

        // 自动添加创建者为所有者
        TbDataSpaceMember ownerMember = new TbDataSpaceMember();
        ownerMember.setfId(UUID.randomUUID().toString().replace("-", ""));
        ownerMember.setfSpaceId(space.getfId());
        ownerMember.setfOrganizationId(orgId);  // 使用已生成的orgId
        ownerMember.setfOrganizationName(dto.getOrganizationName() != null ? dto.getOrganizationName() : "未知");
        ownerMember.setfRole(MemberRole.OWNER.getCode());
        ownerMember.setfStatus(1);
        ownerMember.setfJoinTime(LocalDateTime.now());
        ownerMember.setfTenantId(dto.getTenantId());
        ownerMember.setfCreateTime(LocalDateTime.now());
        ownerMember.setfUpdateTime(LocalDateTime.now());
        ownerMember.setfDeleteMark(0);
        memberMapper.insert(ownerMember);

        return space;
    }

    @Override
    @Transactional
    public TbDataSpace updateSpace(String id, DataSpaceDTO dto) {
        TbDataSpace space = dataSpaceMapper.selectById(id);
        if (space == null) {
            throw new BusinessException("数据空间不存在");
        }
        space.setfSpaceName(dto.getSpaceName());
        space.setfSpaceDesc(dto.getSpaceDesc());
        space.setfSpaceType(dto.getSpaceType());
        if (dto.getOwnerId() != null && !dto.getOwnerId().isEmpty()) {
            space.setfOwnerId(dto.getOwnerId());
        }
        if (dto.getOwnerName() != null) {
            space.setfOwnerName(dto.getOwnerName());
        }
        if (dto.getOrganizationId() != null && !dto.getOrganizationId().isEmpty()) {
            space.setfOrganizationId(dto.getOrganizationId());
        }
        if (dto.getOrganizationName() != null) {
            space.setfOrganizationName(dto.getOrganizationName());
        }
        space.setfUpdateTime(LocalDateTime.now());
        dataSpaceMapper.updateById(space);
        return space;
    }

    @Override
    @Transactional
    public void deleteSpace(String id) {
        TbDataSpace space = dataSpaceMapper.selectById(id);
        if (space == null) {
            throw new BusinessException("数据空间不存在");
        }
        dataSpaceMapper.deleteById(id);
    }

    @Override
    @Transactional
    public TbDataSpace approveSpace(String id, Integer status) {
        TbDataSpace space = dataSpaceMapper.selectById(id);
        if (space == null) {
            throw new BusinessException("数据空间不存在");
        }
        space.setfStatus(status);
        space.setfUpdateTime(LocalDateTime.now());
        dataSpaceMapper.updateById(space);
        return space;
    }

    @Override
    @Transactional
    public TbDataSpace freezeSpace(String id, Integer status) {
        TbDataSpace space = dataSpaceMapper.selectById(id);
        if (space == null) {
            throw new BusinessException("数据空间不存在");
        }
        space.setfStatus(status);
        space.setfUpdateTime(LocalDateTime.now());
        dataSpaceMapper.updateById(space);
        return space;
    }

    @Override
    public List<TbDataSpace> getUserSpaces(String userId) {
        // 查询用户作为成员的所有空间（现在按机构管理，返回所有空间）
        LambdaQueryWrapper<TbDataSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDataSpace::getfDeleteMark, 0);
        return dataSpaceMapper.selectList(wrapper);
    }

    // ==================== 成员管理 ====================

    @Override
    public PageResult<TbDataSpaceMember> getMemberPage(int currentPage, int pageSize,
            String spaceId, Integer role, Integer status) {
        Page<TbDataSpaceMember> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDataSpaceMember> wrapper = new LambdaQueryWrapper<>();
        if (spaceId != null && !spaceId.isEmpty()) {
            wrapper.eq(TbDataSpaceMember::getfSpaceId, spaceId);
        }
        if (role != null) {
            wrapper.eq(TbDataSpaceMember::getfRole, role);
        }
        if (status != null) {
            wrapper.eq(TbDataSpaceMember::getfStatus, status);
        }
        wrapper.orderByAsc(TbDataSpaceMember::getfRole);
        long total = memberMapper.selectCount(wrapper);
        IPage<TbDataSpaceMember> result = memberMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), total, currentPage, pageSize);
    }

    @Override
    public TbDataSpaceMember getMemberById(String id) {
        return memberMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbDataSpaceMember addMember(DataSpaceMemberDTO dto) {
        // 检查是否已存在（按机构ID检查）
        LambdaQueryWrapper<TbDataSpaceMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDataSpaceMember::getfSpaceId, dto.getSpaceId())
                .eq(TbDataSpaceMember::getfOrganizationId, dto.getOrganizationId())
                .eq(TbDataSpaceMember::getfDeleteMark, 0);
        if (memberMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该机构已在空间中");
        }

        TbDataSpaceMember member = new TbDataSpaceMember();
        member.setfId(UUID.randomUUID().toString().replace("-", ""));
        member.setfSpaceId(dto.getSpaceId());
        member.setfOrganizationId(dto.getOrganizationId());
        member.setfOrganizationName(dto.getOrganizationName());
        member.setfRole(dto.getRole() != null ? dto.getRole() : MemberRole.MEMBER.getCode());
        member.setfStatus(0); // 待审核
        member.setfApplyReason(dto.getApplyReason());
        member.setfTenantId(dto.getTenantId());
        member.setfCreateTime(LocalDateTime.now());
        member.setfUpdateTime(LocalDateTime.now());
        member.setfDeleteMark(0);

        memberMapper.insert(member);

        // 更新空间成员数量
        updateSpaceMemberCount(dto.getSpaceId());

        return member;
    }

    @Override
    @Transactional
    public TbDataSpaceMember updateMemberRole(String id, Integer role) {
        TbDataSpaceMember member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }
        if (member.getfRole() == MemberRole.OWNER.getCode()) {
            throw new BusinessException("不能修改所有者角色");
        }
        member.setfRole(role);
        member.setfUpdateTime(LocalDateTime.now());
        memberMapper.updateById(member);
        return member;
    }

    @Override
    @Transactional
    public TbDataSpaceMember approveMember(String id, Integer status) {
        TbDataSpaceMember member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }
        member.setfStatus(status);
        if (status == 1) {
            member.setfJoinTime(LocalDateTime.now());
        }
        member.setfUpdateTime(LocalDateTime.now());
        memberMapper.updateById(member);
        return member;
    }

    @Override
    @Transactional
    public void removeMember(String id) {
        TbDataSpaceMember member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }
        if (member.getfRole() == MemberRole.OWNER.getCode()) {
            throw new BusinessException("不能移除所有者");
        }
        memberMapper.deleteById(id);

        // 更新空间成员数量
        updateSpaceMemberCount(member.getfSpaceId());
    }

    @Override
    public TbDataSpaceMember getMemberRole(String spaceId, String userId) {
        // 按机构查询成员角色（现在按机构管理，不再按用户）
        LambdaQueryWrapper<TbDataSpaceMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDataSpaceMember::getfSpaceId, spaceId)
                .eq(TbDataSpaceMember::getfStatus, 1)
                .eq(TbDataSpaceMember::getfDeleteMark, 0);
        return memberMapper.selectOne(wrapper);
    }

    // ==================== 资源管理 ====================

    @Override
    public PageResult<TbDataSpaceResource> getResourcePage(int currentPage, int pageSize,
            String spaceId, String resourceType) {
        Page<TbDataSpaceResource> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDataSpaceResource> wrapper = new LambdaQueryWrapper<>();
        if (spaceId != null && !spaceId.isEmpty()) {
            wrapper.eq(TbDataSpaceResource::getfSpaceId, spaceId);
        }
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq(TbDataSpaceResource::getfResourceType, resourceType);
        }
        wrapper.orderByDesc(TbDataSpaceResource::getfCreateTime);
        long total = resourceMapper.selectCount(wrapper);
        IPage<TbDataSpaceResource> result = resourceMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), total, currentPage, pageSize);
    }

    @Override
    @Transactional
    public TbDataSpaceResource addResource(DataSpaceResourceDTO dto) {
        // 检查是否已存在
        LambdaQueryWrapper<TbDataSpaceResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDataSpaceResource::getfSpaceId, dto.getSpaceId())
                .eq(TbDataSpaceResource::getfResourceId, dto.getResourceId())
                .eq(TbDataSpaceResource::getfDeleteMark, 0);
        if (resourceMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该资源已在空间中");
        }

        TbDataSpaceResource resource = new TbDataSpaceResource();
        resource.setfId(UUID.randomUUID().toString().replace("-", ""));
        resource.setfSpaceId(dto.getSpaceId());
        resource.setfResourceType(dto.getResourceType());
        resource.setfResourceId(dto.getResourceId());
        resource.setfResourceName(dto.getResourceName());
        resource.setfResourceDesc(dto.getResourceDesc());
        resource.setfAccessLevel(dto.getAccessLevel() != null ? dto.getAccessLevel() : 1);
        resource.setfTenantId(dto.getTenantId());
        resource.setfCreateTime(LocalDateTime.now());
        resource.setfUpdateTime(LocalDateTime.now());
        resource.setfDeleteMark(0);

        resourceMapper.insert(resource);

        // 更新空间资源数量
        updateSpaceResourceCount(dto.getSpaceId());

        return resource;
    }

    @Override
    @Transactional
    public TbDataSpaceResource updateResourceAccess(String id, Integer accessLevel) {
        TbDataSpaceResource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        resource.setfAccessLevel(accessLevel);
        resource.setfUpdateTime(LocalDateTime.now());
        resourceMapper.updateById(resource);
        return resource;
    }

    @Override
    @Transactional
    public void removeResource(String id) {
        TbDataSpaceResource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        resourceMapper.deleteById(id);

        // 更新空间资源数量
        updateSpaceResourceCount(resource.getfSpaceId());
    }

    @Override
    public List<TbDataSpaceResource> getUserAccessibleResources(String userId, String resourceType) {
        // 获取用户参与的空间
        List<TbDataSpace> userSpaces = getUserSpaces(userId);
        if (userSpaces.isEmpty()) {
            return List.of();
        }

        List<String> spaceIds = userSpaces.stream().map(TbDataSpace::getfId).toList();

        LambdaQueryWrapper<TbDataSpaceResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TbDataSpaceResource::getfSpaceId, spaceIds)
                .eq(TbDataSpaceResource::getfDeleteMark, 0);
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq(TbDataSpaceResource::getfResourceType, resourceType);
        }
        return resourceMapper.selectList(wrapper);
    }

    // ==================== 辅助方法 ====================

    private void updateSpaceMemberCount(String spaceId) {
        TbDataSpace space = dataSpaceMapper.selectById(spaceId);
        if (space != null) {
            LambdaQueryWrapper<TbDataSpaceMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbDataSpaceMember::getfSpaceId, spaceId)
                    .eq(TbDataSpaceMember::getfStatus, 1)
                    .eq(TbDataSpaceMember::getfDeleteMark, 0);
            long count = memberMapper.selectCount(wrapper);
            space.setfMemberCount((int) count);
            space.setfUpdateTime(LocalDateTime.now());
            dataSpaceMapper.updateById(space);
        }
    }

    private void updateSpaceResourceCount(String spaceId) {
        TbDataSpace space = dataSpaceMapper.selectById(spaceId);
        if (space != null) {
            LambdaQueryWrapper<TbDataSpaceResource> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TbDataSpaceResource::getfSpaceId, spaceId)
                    .eq(TbDataSpaceResource::getfDeleteMark, 0);
            long count = resourceMapper.selectCount(wrapper);
            space.setfResourceCount((int) count);
            space.setfUpdateTime(LocalDateTime.now());
            dataSpaceMapper.updateById(space);
        }
    }
}