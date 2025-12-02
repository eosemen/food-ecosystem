package model;

public class Organization {
    private int id;
    private String name;
    private String type;
    private int enterpriseId;

    public Organization() {
    }

    public Organization(int id, String name, String type, int enterpriseId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.enterpriseId = enterpriseId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(int enterpriseId) {
        this.enterpriseId = enterpriseId;
    }
}
