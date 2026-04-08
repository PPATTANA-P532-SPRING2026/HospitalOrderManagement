package com.pm.ordersystem.access;

import com.pm.ordersystem.model.user.Clinician;
import com.pm.ordersystem.resource.ClinicianStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClinicianAccess {

    private final ClinicianStore clinicianStore;

    public ClinicianAccess(ClinicianStore clinicianStore) {
        this.clinicianStore = clinicianStore;
    }

    public void registerClinician(Clinician clinician) {
        clinicianStore.add(clinician);
    }

    public List<Clinician> listAllClinicians() {
        return clinicianStore.getAll();
    }

    public Optional<Clinician> findById(String id) {
        return clinicianStore.getAll()
                .stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    public Optional<Clinician> findByName(String name) {
        return clinicianStore.getAll()
                .stream()
                .filter(c -> c.getName().equals(name))
                .findFirst();
    }

    public boolean exists(String name) {
        return findByName(name).isPresent();
    }
}