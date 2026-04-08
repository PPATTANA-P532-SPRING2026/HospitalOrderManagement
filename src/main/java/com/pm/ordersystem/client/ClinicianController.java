package com.pm.ordersystem.client;

import com.pm.ordersystem.access.ClinicianAccess;
import com.pm.ordersystem.model.user.Clinician;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clinicians")
public class ClinicianController {

    private final ClinicianAccess clinicianAccess;

    public ClinicianController(ClinicianAccess clinicianAccess) {
        this.clinicianAccess = clinicianAccess;
    }

    // ── GET /api/clinicians ───────────────────────────────────────────
    @GetMapping
    public List<Clinician> getAllClinicians() {
        return clinicianAccess.listAllClinicians();
    }

    // ── POST /api/clinicians ──────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> registerClinician(
            @RequestBody Map<String, String> body) {
        try {
            String name       = body.get("name");
            String department = body.get("department");

            if (name == null || name.isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Clinician name cannot be empty");
            }

            if (clinicianAccess.exists(name)) {
                return ResponseEntity.badRequest()
                        .body("Clinician already exists: " + name);
            }

            Clinician clinician = new Clinician(name, department);
            clinicianAccess.registerClinician(clinician);
            return ResponseEntity.ok(clinician);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}