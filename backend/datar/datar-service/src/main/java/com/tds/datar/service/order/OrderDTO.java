package com.tds.datar.service.order;

/**
 * 订单DTO
 */
public class OrderDTO {

    private String productId;
    private String buyerTenantId;
    private String sellerTenantId;
    private String tenantId;
    private String buyerSpaceId;
    private String sellerSpaceId;

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getBuyerTenantId() { return buyerTenantId; }
    public void setBuyerTenantId(String buyerTenantId) { this.buyerTenantId = buyerTenantId; }
    public String getSellerTenantId() { return sellerTenantId; }
    public void setSellerTenantId(String sellerTenantId) { this.sellerTenantId = sellerTenantId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getBuyerSpaceId() { return buyerSpaceId; }
    public void setBuyerSpaceId(String buyerSpaceId) { this.buyerSpaceId = buyerSpaceId; }
    public String getSellerSpaceId() { return sellerSpaceId; }
    public void setSellerSpaceId(String sellerSpaceId) { this.sellerSpaceId = sellerSpaceId; }
}