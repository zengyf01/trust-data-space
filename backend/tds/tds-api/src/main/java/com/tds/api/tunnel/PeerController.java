package com.tds.api.tunnel;

import com.tds.common.core.ApiResponse;
import com.tds.service.tunnel.IPeerCommunicationService;
import com.tds.service.tunnel.IPeerCommunicationService.PeerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 对等通信API控制器
 */
@RestController
@RequestMapping("/peer")
public class PeerController {

    @Autowired
    private IPeerCommunicationService peerCommunicationService;

    /**
     * 发起对等连接
     */
    @PostMapping("/connect")
    public ApiResponse<PeerSession> initiatePeerConnection(@RequestBody PeerConnectDTO dto) {
        PeerSession session = peerCommunicationService.initiatePeerConnection(
            dto.getInitiatorSn(),
            dto.getTargetSn()
        );
        return ApiResponse.success(session);
    }

    /**
     * 确认对等连接(被邀请方调用)
     */
    @PostMapping("/{peerSessionId}/confirm")
    public ApiResponse<Map<String, Object>> confirmPeerConnection(
            @PathVariable String peerSessionId,
            @RequestBody Map<String, String> body) {
        String targetSign = body.get("targetSign");
        boolean success = peerCommunicationService.confirmPeerConnection(peerSessionId, targetSign);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("peerSessionId", peerSessionId);
        result.put("message", success ? "对等连接已建立" : "对等连接确认失败");
        return ApiResponse.success(result);
    }

    /**
     * 通过对等连接发送数据
     */
    @PostMapping("/{peerSessionId}/data")
    public ApiResponse<Map<String, Object>> sendPeerData(
            @PathVariable String peerSessionId,
            @RequestBody Map<String, String> body) {
        String dataBase64 = body.get("data");
        byte[] data = java.util.Base64.getDecoder().decode(dataBase64);
        byte[] response = peerCommunicationService.sendPeerData(peerSessionId, data);

        Map<String, Object> result = new HashMap<>();
        result.put("response", java.util.Base64.getEncoder().encodeToString(response));
        return ApiResponse.success(result);
    }

    /**
     * 关闭对等连接
     */
    @DeleteMapping("/{peerSessionId}")
    public ApiResponse<Void> closePeerSession(@PathVariable String peerSessionId) {
        peerCommunicationService.closePeerSession(peerSessionId);
        return ApiResponse.success();
    }

    /**
     * 获取对等会话状态
     */
    @GetMapping("/{peerSessionId}")
    public ApiResponse<PeerSession> getPeerSession(@PathVariable String peerSessionId) {
        PeerSession session = peerCommunicationService.getPeerSession(peerSessionId);
        return ApiResponse.success(session);
    }

    /**
     * 对等连接请求DTO
     */
    public static class PeerConnectDTO {
        private String initiatorSn;
        private String targetSn;

        public String getInitiatorSn() { return initiatorSn; }
        public void setInitiatorSn(String initiatorSn) { this.initiatorSn = initiatorSn; }
        public String getTargetSn() { return targetSn; }
        public void setTargetSn(String targetSn) { this.targetSn = targetSn; }
    }
}
