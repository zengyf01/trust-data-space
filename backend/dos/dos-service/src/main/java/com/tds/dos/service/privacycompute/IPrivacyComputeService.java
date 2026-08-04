package com.tds.dos.service.privacycompute;

import java.util.List;
import java.util.Map;

/**
 * 隐私计算服务接口
 * 支持 PSI、MPC、横向联邦等隐私计算任务
 */
public interface IPrivacyComputeService {

    // ==================== 任务管理 ====================

    /**
     * 创建隐私计算任务
     * @param params 任务参数
     * @return 任务ID
     */
    String createTask(Map<String, Object> params);

    /**
     * 执行任务
     * @param taskId 任务ID
     */
    void executeTask(String taskId);

    /**
     * 查询任务状态
     * @param taskId 任务ID
     * @return 状态码: 1=CREATED, 2=PENDING, 3=RUNNING, 4=COMPLETED, 5=FAILED, 6=CANCELLED
     */
    Integer queryTaskStatus(String taskId);

    /**
     * 获取任务结果
     * @param taskId 任务ID
     * @return 结果JSON
     */
    String getTaskResult(String taskId);

    /**
     * 获取任务生成的代码（Python代码）
     * @param taskId 任务ID
     * @return 生成的代码
     */
    String getTaskCode(String taskId);

    /**
     * 取消任务
     * @param taskId 任务ID
     */
    void cancelTask(String taskId);

    /**
     * 删除任务
     * @param taskId 任务ID
     */
    void deleteTask(String taskId);

    /**
     * 根据ID获取任务详情
     * @param taskId 任务ID
     * @return 任务实体
     */
    com.tds.dos.dal.msp.entity.TbTask getTaskById(String taskId);

    /**
     * 获取任务列表
     * @param page 页码
     * @param size 每页大小
     * @return 任务列表分页结果
     */
    Map<String, Object> listTasks(int page, int size);

    /**
     * 下载 PSI 任务一方的结果 CSV（容器内 /tmp/psi_result_{taskId}_{party}.csv）
     * @param taskId PSI 任务 ID
     * @param party  alice 或 bob
     * @return CSV 字节内容；任务未完成或文件不存在时抛异常
     */
    byte[] downloadPsiResultFile(String taskId, String party);

    /**
     * 下载 FL/VFL 任务一方的模型文件（容器内 /tmp/fl_model_{taskId}.pkl 或 /tmp/vfl_model_{taskId}.pkl）
     * @param taskId FL/VFL 任务 ID
     * @param party  alice 或 bob
     * @return 模型文件字节内容；任务未完成或文件不存在时抛异常
     */
    byte[] downloadModelFile(String taskId, String party);

    // ==================== PSI 求交 ====================

    /**
     * 执行 PSI 求交任务
     * @param taskName 任务名称
     * @param partyADataPath A方数据路径
     * @param partyBDataPath B方数据路径
     * @param keyColumn 关联键列
     * @param params 其他参数
     * @return 任务ID
     */
    String executePsiTask(String taskName, String partyADataPath, String partyBDataPath,
                          String keyColumn, Map<String, Object> params);

    /**
     * 执行 PSI 求交并等待结果
     * @param taskName 任务名称
     * @param partyADataPath A方数据路径
     * @param partyBDataPath B方数据路径
     * @param keyColumn 关联键列
     * @param params 额外参数（应包含 partyANodeId/partyBNodeId，避免按在线节点顺序回退）
     * @param timeoutSeconds 超时秒数
     * @return 求交结果
     */
    Map<String, Object> executePsiTaskWithResult(String taskName, String partyADataPath,
                                                  String partyBDataPath, String keyColumn,
                                                  Map<String, Object> params, int timeoutSeconds);


    // ==================== PIR 隐匿查询 ====================

    /**
     * 创建 PIR 任务（只创建，不执行，不等待结果）
     * @param params PIR任务参数
     * @return 任务ID
     */
    String createPirTask(Map<String, Object> params);

    /**
     * 执行 PIR 隐匿查询并返回结果（创建 + 执行 + 等待结果，向后兼容）
     * @param params PIR任务参数
     * @return PIR查询结果
     */
    Map<String, Object> executePirTaskWithResult(Map<String, Object> params);

    // ==================== MPC 多方计算 ====================

    /**
     * 创建 MPC 任务（只创建，不执行）
     * @param taskName 任务名称
     * @param participants 参与方列表
     * @param algorithm 算法名称
     * @param params 参数
     * @return 任务ID
     */
    String createMpcTask(String taskName, List<String> participants,
                         String algorithm, Map<String, Object> params);

    // ==================== 横向联邦 ====================

    /**
     * 创建横向联邦任务（只创建，不执行）
     * @param taskName 任务名称
     * @param participants 参与方列表
     * @param labelColumn 标签列
     * @param featureColumns 特征列列表
     * @param params 其他参数
     * @return 任务ID
     */
    String createFederatedLearningTask(String taskName, List<String> participants,
                                        String labelColumn, List<String> featureColumns,
                                        Map<String, Object> params);

    /**
     * 创建纵向联邦学习任务（只创建，不执行）
     * @param taskName 任务名称
     * @param participants 参与方列表
     * @param labelColumn 标签列
     * @param featureColumns 特征列映射 (partyId -> [columns])
     * @param params 其他参数
     * @return 任务ID
     */
    String createVerticalFlTask(String taskName, List<String> participants,
                                 String labelColumn, Map<String, List<String>> featureColumns,
                                 Map<String, Object> params);

    // ==================== DAG 任务 ====================

    /**
     * 创建 DAG 任务（只保存，不执行）
     * @param dagName DAG名称
     * @param dagDefinition DAG定义JSON，包含nodes和edges
     * @param participants 参与方列表
     * @param params 其他参数
     * @return 任务ID
     */
    String createDagTask(String dagName, String dagDefinition, List<String> participants, Map<String, Object> params);

    /**
     * 执行已保存的 DAG 任务
     * @param taskId 任务ID
     */
    void executeDagTask(String taskId);

    /**
     * 执行 DAG 任务（保存并立即执行）
     * @param dagName DAG名称
     * @param dagDefinition DAG定义JSON
     * @param participants 参与方列表
     * @param params 其他参数
     * @return 任务ID
     */
    String submitDagTask(String dagName, String dagDefinition, List<String> participants, Map<String, Object> params);

    // ==================== 节点管理 ====================

    /**
     * 注册计算节点
     * @param nodeId 节点ID
     * @param nodeName 节点名称
     * @param endpoint 节点端点
     * @param nodeMode 节点模式 (RAY/KUSCIA)
     * @return 注册结果
     */
    String registerNode(String nodeId, String nodeName, String endpoint, String nodeMode);

    /**
     * 节点心跳
     * @param nodeId 节点ID
     */
    void nodeHeartbeat(String nodeId);

    /**
     * 获取节点列表
     * @param page 页码
     * @param size 每页大小
     * @param status 节点状态筛选 (可选)
     * @return 节点列表分页结果
     */
    Map<String, Object> listNodes(int page, int size, Integer status);

    /**
     * 注销节点
     * @param nodeId 节点ID
     * @return 是否成功
     */
    boolean unregisterNode(String nodeId);

    /**
     * 更新节点名称
     * @param nodeId 节点ID
     * @param nodeName 新名称
     */
    void updateNodeName(String nodeId, String nodeName);

    // ==================== 数据源管理 ====================

    /**
     * 注册数据源
     * @param datasourceId 数据源ID
     * @param datasourceType 数据源类型 (MYSQL/POSTGRESQL/CSV/FILE)
     * @param connectionInfo 连接信息
     * @return 注册结果
     */
    String registerDatasource(String datasourceId, String datasourceType, Map<String, String> connectionInfo);

    /**
     * 获取数据源信息
     * @param datasourceId 数据源ID
     * @return 数据源信息
     */
    Map<String, Object> getDatasource(String datasourceId);
}