package com.river.sand.controller;

import com.river.sand.common.Result;
import com.river.sand.entity.MiningDeclaration;
import com.river.sand.enums.DeclarationStatus;
import com.river.sand.service.MiningDeclarationService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/declarations")
public class DeclarationController {

    private final MiningDeclarationService declarationService;

    public DeclarationController(MiningDeclarationService declarationService) {
        this.declarationService = declarationService;
    }

    @GetMapping
    public Result<List<MiningDeclaration>> getDeclarations(
            @RequestParam(required = false) Long permitId,
            @RequestParam(required = false) Long holderId,
            @RequestParam(required = false) DeclarationStatus status) {
        if (permitId != null) {
            return Result.success(declarationService.getDeclarationsByPermit(permitId));
        }
        if (holderId != null) {
            return Result.success(declarationService.getDeclarationsByHolder(holderId));
        }
        if (status != null) {
            return Result.success(declarationService.getDeclarationsByStatus(status));
        }
        return Result.error("请提供查询条件");
    }

    @GetMapping("/{id}")
    public Result<MiningDeclaration> getDeclarationById(@PathVariable Long id) {
        return declarationService.getDeclarationById(id)
                .map(Result::success)
                .orElse(Result.error("申报单不存在"));
    }

    @GetMapping("/no/{declarationNo}")
    public Result<MiningDeclaration> getDeclarationByNo(@PathVariable String declarationNo) {
        return declarationService.getDeclarationByNo(declarationNo)
                .map(Result::success)
                .orElse(Result.error("申报单不存在"));
    }

    @PostMapping
    public Result<MiningDeclaration> createDeclaration(
            @RequestBody MiningDeclaration declaration,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String userName) {
        return Result.success(declarationService.createDeclaration(declaration, userId, userName));
    }

    @PostMapping("/{id}/submit")
    public Result<MiningDeclaration> submitDeclaration(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String userName) {
        return Result.success(declarationService.submitDeclaration(id, userId, userName));
    }

    @PostMapping("/{id}/verify")
    public Result<MiningDeclaration> verifyDeclaration(
            @PathVariable Long id,
            @RequestBody Map<String, Object> verifyData,
            @RequestHeader("X-User-Id") Long verifierId,
            @RequestHeader("X-User-Name") String verifierName) {
        BigDecimal weighedVolume = new BigDecimal(verifyData.get("weighedVolume").toString());
        String verifyRemark = (String) verifyData.get("verifyRemark");
        return Result.success(declarationService.verifyDeclaration(
                id, weighedVolume, verifyRemark, verifierId, verifierName));
    }

    @PostMapping("/{id}/cancel")
    public Result<MiningDeclaration> cancelDeclaration(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String userName) {
        return Result.success(declarationService.cancelDeclaration(id, userId, userName));
    }
}
