package com.pm.ordersystem.client;

import com.pm.ordersystem.access.StaffAccess;
import com.pm.ordersystem.model.staff.StaffMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffAccess staffAccess;

    public StaffController(StaffAccess staffAccess) {
        this.staffAccess = staffAccess;
    }

    // ── GET /api/staff — list all staff
    @GetMapping
    public List<StaffMember> getAllStaff() {
        return staffAccess.listAllStaff();
    }

    // ── POST /api/staff — register new staff member ───────────────────
    @PostMapping
    public ResponseEntity<?> registerStaff(
            @RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            String role = body.get("role");

            if (name == null || name.isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Staff name cannot be empty");
            }

            if (staffAccess.exists(name)) {
                return ResponseEntity.badRequest()
                        .body("Staff member already exists: " + name);
            }

            StaffMember member = new StaffMember(name, role);
            staffAccess.registerStaff(member);
            return ResponseEntity.ok(member);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}