package com.wms.model;

import java.io.Serializable;

public class Staff implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long staffId;
    private String username;
    private String fullName;
    private String role; // 'ADVISOR', 'MECHANIC', 'ADMIN'
    private String phone;
    private Boolean active;

    public Staff() {}

    public Staff(Long staffId, String username, String fullName, String role, String phone, Boolean active) {
        this.staffId = staffId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.phone = phone;
        this.active = active;
    }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
