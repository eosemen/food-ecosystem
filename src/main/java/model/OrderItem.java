package model;

import java.math.BigDecimal;

public class OrderItem {
    private int id;
    private int orderId;
    private String menuItemName;
    private BigDecimal price;
    private int quantity;

    public OrderItem() {}

    public OrderItem(int id, int orderId, String menuItemName, BigDecimal price, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemName = menuItemName;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getMenuItemName() { return menuItemName; }
    public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }

    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
