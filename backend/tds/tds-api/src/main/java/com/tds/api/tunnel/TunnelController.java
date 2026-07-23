package com.tds.api.tunnel;

import com.tds.common.core.ApiResponse;
import com.tds.common.dto.tunnel.TunnelCommandResult;
import com.tds.common.dto.tunnel.TunnelRegistrationResult;
import com.tds.common.dto.tunnel.TunnelSession;
import com.tds.common.enums.TunnelStatus;
import com.tds.service.tunnel.ConnectorTunnelService;
import com.tds.service.tunnel.IGmTunnelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 隧道API控制器
 */
@RestController
@RequestMapping("/tunnel")
public class TunnelController {

    @Autowired
    private IGmTunnelManager tunnelManager;

    @Autowired
    private ConnectorTunnelService connectorTunnelService;

    /**
     * 连接器注册并建立隧道
     */
    @PostMapping("/register")
    public ApiResponse<TunnelRegistrationResult> registerAndCreateTunnel(@RequestBody TunnelRegisterDTO dto) {
        TunnelRegistrationResult result = connectorTunnelService.registerAndCreateTunnel(
            dto.getSn(),
            dto.getPublicKey(),
            dto.getCertificate(),
            dto.getConnectorType(),
            dto.getInstitutionId(),
            dto.getSpaceId()
        );
        return ApiResponse.success(result);
    }

    /**
     * 完成SM2双向认证
     */
    @PostMapping("/{sessionId}/auth")
    public ApiResponse<Map<String, Object>> completeMutualAuth(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String clientSign = body.get("clientSign");
        boolean success = connectorTunnelService.completeMutualAuth(sessionId, clientSign);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "认证成功" : "认证失败");
        return ApiResponse.success(result);
    }

    /**
     * 通过隧道下发命令
     */
    @PostMapping("/{sessionId}/command")
    public ApiResponse<TunnelCommandResult> executeCommand(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String command = body.get("command");
        TunnelCommandResult result = tunnelManager.executeCommand(sessionId, command);
        return ApiResponse.success(result);
    }

    /**
     * 关闭隧道会话
     */
    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> closeTunnel(@PathVariable String sessionId) {
        tunnelManager.closeTunnelSession(sessionId);
        return ApiResponse.success();
    }

    /**
     * 获取隧道状态
     */
    @GetMapping("/{sn}/status")
    public ApiResponse<Map<String, Object>> getTunnelStatus(@PathVariable String sn) {
        TunnelStatus status = connectorTunnelService.getTunnelStatus(sn);
        TunnelSession session = connectorTunnelService.getActiveTunnel(sn);

        Map<String, Object> result = new HashMap<>();
        result.put("sn", sn);
        result.put("status", status.getDescription());
        result.put("statusCode", status.getCode());
        if (session != null) {
            result.put("sessionId", session.getSessionId());
            result.put("tunnelPort", session.getTunnelPort());
            result.put("tunnelHost", session.getTunnelHost());
        }
        return ApiResponse.success(result);
    }

    /**
     * 心跳保活
     */
    @PostMapping("/{sessionId}/heartbeat")
    public ApiResponse<Void> heartbeat(@PathVariable String sessionId) {
        tunnelManager.keepAlive(sessionId);
        return ApiResponse.success();
    }

    /**
     * 获取服务端公钥
     */
    @GetMapping("/server/publickey")
    public ApiResponse<Map<String, String>> getServerPublicKey() {
        Map<String, String> result = new HashMap<>();
        result.put("publicKey", connectorTunnelService.getServerPublicKey());
        return ApiResponse.success(result);
    }

    /**
     * 隧道注册请求DTO
     */
    public static class TunnelRegisterDTO {
        private String sn;
        private String publicKey;
        private String certificate;
        private Integer connectorType;
        private String institutionId;
        private String spaceId;

        public String getSn() { return sn; }
        public void setSn(String sn) { this.sn = sn; }
        public String getPublicKey() { return publicKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
        public String getCertificate() { return certificate; }
        public void setCertificate(String certificate) { this.certificate = certificate; }
        public Integer getConnectorType() { return connectorType; }
        public void setConnectorType(Integer connectorType) { this.connectorType = connectorType; }
        public String getInstitutionId() { return institutionId; }
        public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
        public String getSpaceId() { return spaceId; }
        public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
    }
}
