package model;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String name;
    private String phone;
    private String email;
    private int organizationId;

    public User() {}

    public User(int id, String username, String password, String role, String name, String phone, String email, int organizationId) {
        this.id = id;
        this.username = username == null ? null : username.trim();
        this.password = password;
        this.role = role;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.organizationId = organizationId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username == null ? null : username.trim(); }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }
}
