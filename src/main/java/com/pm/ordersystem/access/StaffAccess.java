package com.pm.ordersystem.access;

import com.pm.ordersystem.model.staff.StaffMember;
import com.pm.ordersystem.resource.StaffStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class StaffAccess {

    private final StaffStore staffStore;

    public StaffAccess(StaffStore staffStore) {
        this.staffStore = staffStore;
    }

    public void registerStaff(StaffMember member) {
        staffStore.add(member);
    }

    public List<StaffMember> listAllStaff() {
        return staffStore.getAll();
    }

    public Optional<StaffMember> findByName(String name) {
        return staffStore.getAll()
                .stream()
                .filter(s -> s.getName().equals(name))
                .findFirst();
    }

    public boolean exists(String name) {
        return findByName(name).isPresent();
    }
}