package com.pm.ordersystem.resource;

import com.pm.ordersystem.model.user.Clinician;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ClinicianStore {

    private final List<Clinician> clinicians = new ArrayList<>();

    public void add(Clinician clinician) {
        clinicians.add(clinician);
    }

    public List<Clinician> getAll() {
        return Collections.unmodifiableList(clinicians);
    }
}