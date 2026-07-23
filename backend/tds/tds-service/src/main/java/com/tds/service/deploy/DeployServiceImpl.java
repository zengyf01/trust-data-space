package com.tds.service.deploy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.enums.DeployNodeStatus;
import com.tds.common.enums.InstanceStatus;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbDeployInstance;
import com.tds.dal.entity.TbDeployNode;
import com.tds.dal.entity.TbLocalAccount;
import com.tds.dal.mapper.TbDeployInstanceMapper;
import com.tds.dal.mapper.TbDeployNodeMapper;
import com.tds.dal.mapper.TbLocalAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 分布式部署服务实现
 */
@Service
public class DeployServiceImpl implements IDeployService {

    @Autowired
    private TbDeployNodeMapper nodeMapper;

    @Autowired
    private TbDeployInstanceMapper instanceMapper;

    @Autowired
    private TbLocalAccountMapper accountMapper;

    // ==================== 部署节点管理 ====================

    @Override
    public IPage<TbDeployNode> getNodePage(int currentPage, int pageSize, String nodeType, Integer status) {
        Page<TbDeployNode> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDeployNode> wrapper = new LambdaQueryWrapper<>();
        if (nodeType != null && !nodeType.isEmpty()) {
            wrapper.eq(TbDeployNode::getfNodeType, nodeType);
        }
        if (status != null) {
            wrapper.eq(TbDeployNode::getfStatus, status);
        }
        wrapper.orderByDesc(TbDeployNode::getfCreateTime);
        return nodeMapper.selectPage(page, wrapper);
    }

    @Override
    public TbDeployNode getNodeById(String id) {
        return nodeMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbDeployNode createNode(DeployNodeDTO dto) {
        TbDeployNode node = new TbDeployNode();
        node.setfId(UUID.randomUUID().toString().replace("-", ""));
        node.setfNodeCode("NODE" + System.currentTimeMillis());
        node.setfNodeName(dto.getNodeName());
        node.setfNodeType(dto.getNodeType());
        node.setfDeployMode(dto.getDeployMode());
        node.setfIpAddress(dto.getIpAddress());
        node.setfPort(dto.getPort());
        node.setfRegion(dto.getRegion());
        node.setfCpuCores(dto.getCpuCores());
        node.setfMemorySize(dto.getMemorySize());
        node.setfDiskSize(dto.getDiskSize());
        node.setfStatus(DeployNodeStatus.OFFLINE.getCode());
        node.setfTenantId(dto.getTenantId());
        node.setfLastHeartbeat(LocalDateTime.now());
        node.setfCreateTime(LocalDateTime.now());
        node.setfUpdateTime(LocalDateTime.now());
        node.setfDeleteMark(0);

        nodeMapper.insert(node);
        return node;
    }

    @Override
    @Transactional
    public TbDeployNode updateNode(String id, DeployNodeDTO dto) {
        TbDeployNode node = nodeMapper.selectById(id);
        if (node == null) {
            throw new BusinessException("节点不存在");
        }
        node.setfNodeName(dto.getNodeName());
        node.setfIpAddress(dto.getIpAddress());
        node.setfPort(dto.getPort());
        node.setfRegion(dto.getRegion());
        node.setfCpuCores(dto.getCpuCores());
        node.setfMemorySize(dto.getMemorySize());
        node.setfDiskSize(dto.getDiskSize());
        node.setfUpdateTime(LocalDateTime.now());
        nodeMapper.updateById(node);
        return node;
    }

    @Override
    @Transactional
    public void deleteNode(String id) {
        TbDeployNode node = nodeMapper.selectById(id);
        if (node == null) {
            throw new BusinessException("节点不存在");
        }
        node.setfDeleteMark(1);
        node.setfUpdateTime(LocalDateTime.now());
        nodeMapper.updateById(node);
    }

    @Override
    @Transactional
    public TbDeployNode heartbeat(String nodeCode) {
        LambdaQueryWrapper<TbDeployNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDeployNode::getfNodeCode, nodeCode);
        TbDeployNode node = nodeMapper.selectOne(wrapper);
        if (node == null) {
            throw new BusinessException("节点不存在");
        }
        node.setfStatus(DeployNodeStatus.ONLINE.getCode());
        node.setfLastHeartbeat(LocalDateTime.now());
        node.setfUpdateTime(LocalDateTime.now());
        nodeMapper.updateById(node);
        return node;
    }

    @Override
    public List<TbDeployNode> getOnlineNodes() {
        LambdaQueryWrapper<TbDeployNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbDeployNode::getfStatus, DeployNodeStatus.ONLINE.getCode())
                .eq(TbDeployNode::getfDeleteMark, 0);
        return nodeMapper.selectList(wrapper);
    }

    // ==================== 部署实例管理 ====================

    @Override
    public IPage<TbDeployInstance> getInstancePage(int currentPage, int pageSize, String nodeId, String serviceType) {
        Page<TbDeployInstance> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbDeployInstance> wrapper = new LambdaQueryWrapper<>();
        if (nodeId != null && !nodeId.isEmpty()) {
            wrapper.eq(TbDeployInstance::getfNodeId, nodeId);
        }
        if (serviceType != null && !serviceType.isEmpty()) {
            wrapper.eq(TbDeployInstance::getfServiceType, serviceType);
        }
        wrapper.orderByDesc(TbDeployInstance::getfCreateTime);
        return instanceMapper.selectPage(page, wrapper);
    }

    @Override
    public TbDeployInstance getInstanceById(String id) {
        return instanceMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbDeployInstance createInstance(DeployInstanceDTO dto) {
        TbDeployInstance instance = new TbDeployInstance();
        instance.setfId(UUID.randomUUID().toString().replace("-", ""));
        instance.setfInstanceCode("INS" + System.currentTimeMillis());
        instance.setfInstanceName(dto.getInstanceName());
        instance.setfNodeId(dto.getNodeId());
        instance.setfServiceType(dto.getServiceType());
        instance.setfServiceVersion(dto.getServiceVersion());
        instance.setfStatus(InstanceStatus.STOPPED.getCode());
        instance.setfAccessUrl(dto.getAccessUrl());
        instance.setfReplicaCount(dto.getReplicaCount() != null ? dto.getReplicaCount() : 1);
        instance.setfCurrentReplicas(0);
        instance.setfTenantId(dto.getTenantId());
        instance.setfCreateTime(LocalDateTime.now());
        instance.setfUpdateTime(LocalDateTime.now());
        instance.setfDeleteMark(0);

        instanceMapper.insert(instance);
        return instance;
    }

    @Override
    @Transactional
    public TbDeployInstance updateInstance(String id, DeployInstanceDTO dto) {
        TbDeployInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("实例不存在");
        }
        instance.setfInstanceName(dto.getInstanceName());
        instance.setfReplicaCount(dto.getReplicaCount());
        instance.setfAccessUrl(dto.getAccessUrl());
        instance.setfUpdateTime(LocalDateTime.now());
        instanceMapper.updateById(instance);
        return instance;
    }

    @Override
    @Transactional
    public void deleteInstance(String id) {
        TbDeployInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("实例不存在");
        }
        instance.setfDeleteMark(1);
        instance.setfUpdateTime(LocalDateTime.now());
        instanceMapper.updateById(instance);
    }

    @Override
    @Transactional
    public TbDeployInstance startInstance(String id) {
        TbDeployInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("实例不存在");
        }
        if (instance.getfStatus() == InstanceStatus.RUNNING.getCode()) {
            throw new BusinessException("实例已在运行中");
        }
        instance.setfStatus(InstanceStatus.STARTING.getCode());
        instance.setfUpdateTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        // 模拟启动
        instance.setfStatus(InstanceStatus.RUNNING.getCode());
        instance.setfCurrentReplicas(instance.getfReplicaCount());
        instance.setfStartTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        return instance;
    }

    @Override
    @Transactional
    public TbDeployInstance stopInstance(String id) {
        TbDeployInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("实例不存在");
        }
        if (instance.getfStatus() == InstanceStatus.STOPPED.getCode()) {
            throw new BusinessException("实例已停止");
        }
        instance.setfStatus(InstanceStatus.STOPPING.getCode());
        instance.setfUpdateTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        // 模拟停止
        instance.setfStatus(InstanceStatus.STOPPED.getCode());
        instance.setfCurrentReplicas(0);
        instanceMapper.updateById(instance);

        return instance;
    }

    @Override
    @Transactional
    public TbDeployInstance restartInstance(String id) {
        TbDeployInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("实例不存在");
        }
        stopInstance(id);
        return startInstance(id);
    }

    // ==================== 本地账户管理 ====================

    @Override
    public IPage<TbLocalAccount> getAccountPage(int currentPage, int pageSize, String orgId, String accountType) {
        Page<TbLocalAccount> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbLocalAccount> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null && !orgId.isEmpty()) {
            wrapper.eq(TbLocalAccount::getfOrgId, orgId);
        }
        if (accountType != null && !accountType.isEmpty()) {
            wrapper.eq(TbLocalAccount::getfAccountType, accountType);
        }
        wrapper.orderByDesc(TbLocalAccount::getfCreateTime);
        return accountMapper.selectPage(page, wrapper);
    }

    @Override
    public TbLocalAccount getAccountById(String id) {
        return accountMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbLocalAccount createAccount(LocalAccountDTO dto) {
        TbLocalAccount account = new TbLocalAccount();
        account.setfId(UUID.randomUUID().toString().replace("-", ""));
        account.setfAccountCode("ACC" + System.currentTimeMillis());
        account.setfAccountName(dto.getAccountName());
        account.setfAccountType(dto.getAccountType() != null ? dto.getAccountType() : "LOCAL");
        account.setfOrgId(dto.getOrgId());
        account.setfUserId(dto.getUserId());
        account.setfIdCard(dto.getIdCard());
        account.setfPhone(dto.getPhone());
        account.setfEmail(dto.getEmail());
        account.setfAuthMode(dto.getAuthMode());
        account.setfCredential(encryptCredential(dto.getCredential()));
        account.setfIsVerified(dto.getIsVerified() != null ? dto.getIsVerified() : 0);
        account.setfTenantId(dto.getTenantId());
        account.setfCreateTime(LocalDateTime.now());
        account.setfUpdateTime(LocalDateTime.now());
        account.setfDeleteMark(0);

        accountMapper.insert(account);
        return account;
    }

    @Override
    @Transactional
    public TbLocalAccount updateAccount(String id, LocalAccountDTO dto) {
        TbLocalAccount account = accountMapper.selectById(id);
        if (account == null) {
            throw new BusinessException("账户不存在");
        }
        account.setfAccountName(dto.getAccountName());
        account.setfIdCard(dto.getIdCard());
        account.setfPhone(dto.getPhone());
        account.setfEmail(dto.getEmail());
        if (dto.getCredential() != null && !dto.getCredential().isEmpty()) {
            account.setfCredential(encryptCredential(dto.getCredential()));
        }
        account.setfUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);
        return account;
    }

    @Override
    @Transactional
    public void deleteAccount(String id) {
        TbLocalAccount account = accountMapper.selectById(id);
        if (account == null) {
            throw new BusinessException("账户不存在");
        }
        account.setfDeleteMark(1);
        account.setfUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);
    }

    @Override
    public Map<String, Object> localAuth(String accountCode, String credential) {
        LambdaQueryWrapper<TbLocalAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbLocalAccount::getfAccountCode, accountCode);
        TbLocalAccount account = accountMapper.selectOne(wrapper);

        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        String encryptedCredential = encryptCredential(credential);
        if (!encryptedCredential.equals(account.getfCredential())) {
            throw new BusinessException("凭证错误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("accountId", account.getfId());
        result.put("accountCode", account.getfAccountCode());
        result.put("accountName", account.getfAccountName());
        result.put("authMode", account.getfAuthMode());
        result.put("token", "LOCAL_TOKEN_" + UUID.randomUUID().toString().replace("-", ""));

        return result;
    }

    @Override
    public Map<String, Object> switchDeployMode(String mode, String nodeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("mode", mode);
        result.put("nodeId", nodeId);
        result.put("switched", true);
        result.put("timestamp", LocalDateTime.now());
        return result;
    }

    @Override
    public Map<String, Object> offlineOperation(String operation, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", operation);
        result.put("params", params);
        result.put("executed", true);
        result.put("mode", "OFFLINE");
        result.put("timestamp", LocalDateTime.now());
        return result;
    }

    private String encryptCredential(String credential) {
        // 实际应用中应使用BCrypt加密
        return credential;
    }
}