package com.pm.ordersystem.model.staff;

public class StaffMember {

    private final String id;
    private final String name;
    private final String role;

    public StaffMember(String name, String role) {
        this.id   = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.role = role;
    }

    public String getId()   { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
}