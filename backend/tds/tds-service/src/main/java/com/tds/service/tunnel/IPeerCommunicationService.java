package com.tds.service.tunnel;

import com.tds.common.dto.tunnel.TunnelSession;

/**
 * 对等通信服务接口 - 支持连接器间直接通信
 */
public interface IPeerCommunicationService {

    /**
     * 发起对等连接
     * @param initiatorSn 发起方SN
     * @param targetSn 目标方SN
     * @return 对等会话信息
     */
    PeerSession initiatePeerConnection(String initiatorSn, String targetSn);

    /**
     * 确认对等连接(被邀请方调用)
     */
    boolean confirmPeerConnection(String peerSessionId, String targetSign);

    /**
     * 通过对等连接发送数据
     */
    byte[] sendPeerData(String peerSessionId, byte[] data);

    /**
     * 关闭对等连接
     */
    void closePeerSession(String peerSessionId);

    /**
     * 获取对等会话状态
     */
    PeerSession getPeerSession(String peerSessionId);

    /**
     * 对等会话数据结构
     */
    class PeerSession {
        private String peerSessionId;
        private String initiatorSn;
        private String targetSn;
        private String status;  // PENDING/ACTIVE/CLOSED
        private String initiatorSessionId;
        private String targetSessionId;
        private String encryptedKey;  // SM2加密的对等会话密钥
        private long createTime;

        // Getters and Setters
        public String getPeerSessionId() { return peerSessionId; }
        public void setPeerSessionId(String peerSessionId) { this.peerSessionId = peerSessionId; }
        public String getInitiatorSn() { return initiatorSn; }
        public void setInitiatorSn(String initiatorSn) { this.initiatorSn = initiatorSn; }
        public String getTargetSn() { return targetSn; }
        public void setTargetSn(String targetSn) { this.targetSn = targetSn; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getInitiatorSessionId() { return initiatorSessionId; }
        public void setInitiatorSessionId(String initiatorSessionId) { this.initiatorSessionId = initiatorSessionId; }
        public String getTargetSessionId() { return targetSessionId; }
        public void setTargetSessionId(String targetSessionId) { this.targetSessionId = targetSessionId; }
        public String getEncryptedKey() { return encryptedKey; }
        public void setEncryptedKey(String encryptedKey) { this.encryptedKey = encryptedKey; }
        public long getCreateTime() { return createTime; }
        public void setCreateTime(long createTime) { this.createTime = createTime; }
    }
}
