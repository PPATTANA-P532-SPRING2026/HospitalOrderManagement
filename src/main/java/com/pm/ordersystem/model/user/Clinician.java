package com.pm.ordersystem.model.user;

import java.util.UUID;

public class Clinician {

    private final String id;
    private final String name;
    private final String department;

    public Clinician(String name, String department) {
        this.id         = UUID.randomUUID().toString();
        this.name       = name;
        this.department = department;
    }

    public String getId()         { return id; }
    public String getName()       { return name; }
    public String getDepartment() { return department; }
}