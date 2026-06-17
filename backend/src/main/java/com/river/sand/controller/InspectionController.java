package com.river.sand.controller;

import com.river.sand.common.Result;
import com.river.sand.entity.OnsiteInspection;
import com.river.sand.service.OnsiteInspectionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inspections")
public class InspectionController {

    private final OnsiteInspectionService inspectionService;

    public InspectionController(OnsiteInspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping
    public Result<List<OnsiteInspection>> getAllInspections() {
        return Result.success(inspectionService.getAllInspections());
    }

    @GetMapping("/permit/{permitId}")
    public Result<List<OnsiteInspection>> getInspectionsByPermit(@PathVariable Long permitId) {
        return Result.success(inspectionService.getInspectionsByPermit(permitId));
    }

    @GetMapping("/inspector/{inspectorId}")
    public Result<List<OnsiteInspection>> getInspectionsByInspector(@PathVariable Long inspectorId) {
        return Result.success(inspectionService.getInspectionsByInspector(inspectorId));
    }

    @GetMapping("/{id}")
    public Result<OnsiteInspection> getInspectionById(@PathVariable Long id) {
        return inspectionService.getInspectionById(id)
                .map(Result::success)
                .orElse(Result.error("核查记录不存在"));
    }

    @PostMapping
    public Result<OnsiteInspection> createInspection(
            @RequestBody OnsiteInspection inspection,
            @RequestHeader("X-User-Id") Long inspectorId,
            @RequestHeader("X-User-Name") String inspectorName) {
        return Result.success(inspectionService.createInspection(inspection, inspectorId, inspectorName));
    }
}
