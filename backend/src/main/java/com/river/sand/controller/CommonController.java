package com.river.sand.controller;

import com.river.sand.common.Result;
import com.river.sand.entity.AuditTimeline;
import com.river.sand.entity.RiverSection;
import com.river.sand.entity.User;
import com.river.sand.entity.Vessel;
import com.river.sand.enums.Role;
import com.river.sand.service.AuditService;
import com.river.sand.service.MasterDataService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/common")
public class CommonController {

    private final MasterDataService masterDataService;
    private final AuditService auditService;

    public CommonController(MasterDataService masterDataService, AuditService auditService) {
        this.masterDataService = masterDataService;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        User user = masterDataService.getUserByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            return Result.error("密码错误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole().name());
        result.put("roleDesc", user.getRole().getDescription());
        result.put("enterpriseName", user.getEnterpriseName());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("token", "token_" + user.getId() + "_" + System.currentTimeMillis());

        return Result.success(result);
    }

    @GetMapping("/users")
    public Result<List<User>> getAllUsers() {
        return Result.success(masterDataService.getAllUsers());
    }

    @GetMapping("/users/role/{role}")
    public Result<List<User>> getUsersByRole(@PathVariable Role role) {
        return Result.success(masterDataService.getUsersByRole(role));
    }

    @GetMapping("/users/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(masterDataService.getUserById(id));
    }

    @GetMapping("/river-sections")
    public Result<List<RiverSection>> getRiverSections() {
        return Result.success(masterDataService.getAllRiverSections());
    }

    @GetMapping("/river-sections/{id}")
    public Result<RiverSection> getRiverSectionById(@PathVariable Long id) {
        return Result.success(masterDataService.getRiverSectionById(id));
    }

    @GetMapping("/vessels")
    public Result<List<Vessel>> getVessels(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) Long ownerId) {
        if (ownerId != null) {
            return Result.success(masterDataService.getVesselsByOwner(ownerId));
        }
        if (userId != null) {
            User user = masterDataService.getUserById(userId);
            if (user != null && user.getRole() == Role.ENTERPRISE) {
                return Result.success(masterDataService.getVesselsByOwner(userId));
            }
        }
        return Result.success(masterDataService.getAllVessels());
    }

    @GetMapping("/vessels/{id}")
    public Result<Vessel> getVesselById(@PathVariable Long id) {
        return Result.success(masterDataService.getVesselById(id));
    }

    @GetMapping("/audit-timeline")
    public Result<List<AuditTimeline>> getAuditTimeline(
            @RequestParam String businessType,
            @RequestParam Long businessId) {
        return Result.success(auditService.getTimeline(businessType, businessId));
    }

    @GetMapping("/audit-timeline/latest")
    public Result<List<AuditTimeline>> getLatestAuditTimeline(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(auditService.getLatestTimeline(limit));
    }

    @GetMapping("/enums")
    public Result<Map<String, Object>> getEnums() {
        Map<String, Object> enums = new HashMap<>();
        enums.put("roles", Role.values());
        enums.put("applicationStatuses", com.river.sand.enums.ApplicationStatus.values());
        enums.put("permitStatuses", com.river.sand.enums.PermitStatus.values());
        enums.put("inspectionResults", com.river.sand.enums.InspectionResult.values());
        enums.put("penaltyStatuses", com.river.sand.enums.PenaltyStatus.values());
        enums.put("declarationStatuses", com.river.sand.enums.DeclarationStatus.values());
        enums.put("detentionStatuses", com.river.sand.enums.DetentionStatus.values());
        return Result.success(enums);
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }
}
