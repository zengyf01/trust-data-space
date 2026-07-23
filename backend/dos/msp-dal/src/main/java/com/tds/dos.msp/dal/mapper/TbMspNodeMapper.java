package com.tds.dos.msp.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tds.dos.msp.dal.entity.TbMspNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * MSP Node Mapper
 */
@Mapper
@DS("msp")
public interface TbMspNodeMapper extends BaseMapper<TbMspNode> {

    /**
     * 物理删除节点（忽略软删除）
     */
    @Update("DELETE FROM tb_msp_node WHERE f_node_id = #{nodeId}")
    int physicalDeleteByNodeId(@Param("nodeId") String nodeId);
}