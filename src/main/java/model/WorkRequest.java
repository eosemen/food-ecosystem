package model;

import java.time.LocalDateTime;

public class WorkRequest {
    private int id;
    private String type;
    private int senderEnterpriseId;
    private int receiverEnterpriseId;
    private Integer relatedOrderId;
    private String status;
    private String message;
    private LocalDateTime createdAt;

    public WorkRequest() {}

    public WorkRequest(int id, String type, int senderEnterpriseId, int receiverEnterpriseId, Integer relatedOrderId, String status, String message, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.senderEnterpriseId = senderEnterpriseId;
        this.receiverEnterpriseId = receiverEnterpriseId;
        this.relatedOrderId = relatedOrderId;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getSenderEnterpriseId() { return senderEnterpriseId; }
    public void setSenderEnterpriseId(int senderEnterpriseId) { this.senderEnterpriseId = senderEnterpriseId; }
    public int getReceiverEnterpriseId() { return receiverEnterpriseId; }
    public void setReceiverEnterpriseId(int receiverEnterpriseId) { this.receiverEnterpriseId = receiverEnterpriseId; }
    public Integer getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(Integer relatedOrderId) { this.relatedOrderId = relatedOrderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
