package com.river.sand.controller;

import com.river.sand.common.Result;
import com.river.sand.entity.MiningPermit;
import com.river.sand.enums.PermitStatus;
import com.river.sand.service.MiningPermitService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/permits")
public class PermitController {

    private final MiningPermitService permitService;

    public PermitController(MiningPermitService permitService) {
        this.permitService = permitService;
    }

    @GetMapping
    public Result<List<MiningPermit>> getAllPermits(
            @RequestParam(required = false) PermitStatus status,
            @RequestParam(required = false) Long holderId) {
        return Result.success(permitService.getAllPermits(status, holderId));
    }

    @GetMapping("/{id}")
    public Result<MiningPermit> getPermitById(@PathVariable Long id) {
        return permitService.getPermitById(id)
                .map(Result::success)
                .orElse(Result.error("许可证不存在"));
    }

    @GetMapping("/no/{permitNo}")
    public Result<MiningPermit> getPermitByNo(@PathVariable String permitNo) {
        return permitService.getPermitByNo(permitNo)
                .map(Result::success)
                .orElse(Result.error("许可证不存在"));
    }

    @GetMapping("/application/{applicationId}")
    public Result<MiningPermit> getPermitByApplicationId(@PathVariable Long applicationId) {
        return permitService.getPermitByApplicationId(applicationId)
                .map(Result::success)
                .orElse(Result.error("许可证不存在"));
    }

    @PostMapping("/check-expiry")
    public Result<Void> checkAndHandleExpiry() {
        permitService.checkAndHandleExpiry();
        return Result.success();
    }
}
