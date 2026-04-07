package com.pm.ordersystem.resource;

import com.pm.ordersystem.model.staff.StaffMember;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class StaffStore {

    private final List<StaffMember> staff = new ArrayList<>();

    public void add(StaffMember member) {
        staff.add(member);
    }

    public List<StaffMember> getAll() {
        return Collections.unmodifiableList(staff);
    }
}