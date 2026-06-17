package com.river.sand.config;

import com.river.sand.entity.*;
import com.river.sand.enums.ApplicationStatus;
import com.river.sand.enums.InspectionResult;
import com.river.sand.enums.PermitStatus;
import com.river.sand.enums.Role;
import com.river.sand.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class DemoDataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      RiverSectionRepository riverSectionRepository,
                                      VesselRepository vesselRepository,
                                      ForbiddenPeriodRepository forbiddenPeriodRepository,
                                      AnnualQuotaRepository annualQuotaRepository,
                                      MiningApplicationRepository applicationRepository,
                                      MiningPermitRepository permitRepository,
                                      OnsiteInspectionRepository inspectionRepository,
                                      PenaltyClueRepository penaltyClueRepository) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            User enterprise1 = createUser(userRepository, "enterprise1", "123456", "张三", Role.ENTERPRISE,
                    "13800138001", "zhangsan@river.com", "长江砂石有限公司");
            User enterprise2 = createUser(userRepository, "enterprise2", "123456", "李四", Role.ENTERPRISE,
                    "13800138002", "lisi@river.com", "黄河建材有限公司");
            User auditor = createUser(userRepository, "auditor", "123456", "王审核", Role.AUDITOR,
                    "13800138003", "wangsh@river.com", null);
            User enforcer = createUser(userRepository, "enforcer", "123456", "赵执法", Role.ENFORCER,
                    "13800138004", "zhaozf@river.com", null);
            User penaltyHandler = createUser(userRepository, "penalty", "123456", "钱处罚", Role.PENALTY_HANDLER,
                    "13800138005", "qiancf@river.com", null);

            RiverSection section1 = createRiverSection(riverSectionRepository, "RS001", "长江武汉段",
                    "长江", new BigDecimal("30.5"), new BigDecimal("114.3"),
                    new BigDecimal("30.6"), new BigDecimal("114.5"),
                    new BigDecimal("10"), new BigDecimal("50"), new BigDecimal("100000"));
            RiverSection section2 = createRiverSection(riverSectionRepository, "RS002", "黄河郑州段",
                    "黄河", new BigDecimal("34.7"), new BigDecimal("113.6"),
                    new BigDecimal("34.8"), new BigDecimal("113.8"),
                    new BigDecimal("8"), new BigDecimal("40"), new BigDecimal("80000"));

            Vessel vessel1 = createVessel(vesselRepository, "V001", "采砂船01号",
                    enterprise1.getId(), enterprise1.getRealName(),
                    new BigDecimal("500"), new BigDecimal("200"), "采砂船");
            Vessel vessel2 = createVessel(vesselRepository, "V002", "采砂船02号",
                    enterprise1.getId(), enterprise1.getRealName(),
                    new BigDecimal("600"), new BigDecimal("250"), "采砂船");
            Vessel vessel3 = createVessel(vesselRepository, "V003", "运输船01号",
                    enterprise2.getId(), enterprise2.getRealName(),
                    new BigDecimal("1000"), new BigDecimal("500"), "运输船");

            ForbiddenPeriod period1 = new ForbiddenPeriod();
            period1.setStartMonthDay("03-01");
            period1.setEndMonthDay("06-30");
            period1.setDescription("汛期禁采期");
            period1.setEnabled(true);
            forbiddenPeriodRepository.save(period1);

            int currentYear = LocalDate.now().getYear();
            AnnualQuota quota1 = new AnnualQuota();
            quota1.setYear(currentYear);
            quota1.setRiverSectionId(section1.getId());
            quota1.setRiverSectionName(section1.getSectionName());
            quota1.setTotalQuota(new BigDecimal("100000"));
            quota1.setUsedQuota(BigDecimal.ZERO);
            quota1.setRemainingQuota(new BigDecimal("100000"));
            annualQuotaRepository.save(quota1);

            AnnualQuota quota2 = new AnnualQuota();
            quota2.setYear(currentYear);
            quota2.setRiverSectionId(section2.getId());
            quota2.setRiverSectionName(section2.getSectionName());
            quota2.setTotalQuota(new BigDecimal("80000"));
            quota2.setUsedQuota(BigDecimal.ZERO);
            quota2.setRemainingQuota(new BigDecimal("80000"));
            annualQuotaRepository.save(quota2);

            createDemoCompliantPath(applicationRepository, permitRepository,
                    inspectionRepository, section1, vessel1, enterprise1, auditor, enforcer);

            createDemoPenaltyPath(applicationRepository, permitRepository,
                    inspectionRepository, penaltyClueRepository, section2, vessel3,
                    enterprise2, auditor, enforcer, penaltyHandler);
        };
    }

    private User createUser(UserRepository repository, String username, String password,
                           String realName, Role role, String phone, String email,
                           String enterpriseName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        user.setRole(role);
        user.setPhone(phone);
        user.setEmail(email);
        user.setEnterpriseName(enterpriseName);
        return repository.save(user);
    }

    private RiverSection createRiverSection(RiverSectionRepository repository, String code,
                                            String name, String riverName,
                                            BigDecimal startLat, BigDecimal startLon,
                                            BigDecimal endLat, BigDecimal endLon,
                                            BigDecimal length, BigDecimal area,
                                            BigDecimal maxQuota) {
        RiverSection section = new RiverSection();
        section.setSectionCode(code);
        section.setSectionName(name);
        section.setRiverName(riverName);
        section.setStartLatitude(startLat);
        section.setStartLongitude(startLon);
        section.setEndLatitude(endLat);
        section.setEndLongitude(endLon);
        section.setLength(length);
        section.setArea(area);
        section.setMaxAnnualQuota(maxQuota);
        section.setEnabled(true);
        return repository.save(section);
    }

    private Vessel createVessel(VesselRepository repository, String no, String name,
                               Long ownerId, String ownerName,
                               BigDecimal tonnage, BigDecimal capacity, String type) {
        Vessel vessel = new Vessel();
        vessel.setVesselNo(no);
        vessel.setVesselName(name);
        vessel.setOwnerId(ownerId);
        vessel.setOwnerName(ownerName);
        vessel.setTonnage(tonnage);
        vessel.setCapacity(capacity);
        vessel.setVesselType(type);
        vessel.setEnabled(true);
        return repository.save(vessel);
    }

    private void createDemoCompliantPath(MiningApplicationRepository applicationRepository,
                                        MiningPermitRepository permitRepository,
                                        OnsiteInspectionRepository inspectionRepository,
                                        RiverSection section, Vessel vessel,
                                        User enterprise, User auditor, User enforcer) {
        LocalDate startDate = LocalDate.now().plusMonths(1);
        LocalDate endDate = startDate.plusMonths(3);

        MiningApplication app = new MiningApplication();
        app.setApplicationNo("SQDEMO001");
        app.setApplicantId(enterprise.getId());
        app.setEnterpriseName(enterprise.getEnterpriseName());
        app.setRiverSectionId(section.getId());
        app.setRiverSectionName(section.getSectionName());
        app.setVesselIds(vessel.getId().toString());
        app.setVesselNames(vessel.getVesselName());
        app.setStartDate(startDate);
        app.setEndDate(endDate);
        app.setEstimatedVolume(new BigDecimal("5000"));
        app.setDepositAmount(new BigDecimal("50000"));
        app.setStatus(ApplicationStatus.APPROVED);
        app.setRemark("演示数据-合规许可路径");
        app.setReviewOpinion("审核通过，材料齐全，符合许可条件。");
        app.setReviewerId(auditor.getId());
        app.setReviewTime(LocalDateTime.now());
        app.setCreateBy(enterprise.getUsername());
        app.setCreateTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());
        applicationRepository.save(app);

        MiningPermit permit = new MiningPermit();
        permit.setPermitNo("XKDEMO001");
        permit.setApplicationId(app.getId());
        permit.setHolderId(enterprise.getId());
        permit.setHolderName(enterprise.getEnterpriseName());
        permit.setRiverSectionId(section.getId());
        permit.setRiverSectionName(section.getSectionName());
        permit.setVesselIds(vessel.getId().toString());
        permit.setVesselNames(vessel.getVesselName());
        permit.setStartDate(startDate);
        permit.setEndDate(endDate);
        permit.setPermittedVolume(new BigDecimal("5000"));
        permit.setUsedVolume(new BigDecimal("3000"));
        permit.setRemainingVolume(new BigDecimal("2000"));
        permit.setDepositAmount(new BigDecimal("50000"));
        permit.setStatus(PermitStatus.ACTIVE);
        permit.setIssueDate(LocalDateTime.now());
        permit.setUpdateTime(LocalDateTime.now());
        permitRepository.save(permit);

        OnsiteInspection inspection = new OnsiteInspection();
        inspection.setPermitId(permit.getId());
        inspection.setPermitNo(permit.getPermitNo());
        inspection.setInspectorId(enforcer.getId());
        inspection.setInspectorName(enforcer.getRealName());
        inspection.setInspectionTime(LocalDateTime.now());
        inspection.setLatitude(new BigDecimal("30.52"));
        inspection.setLongitude(new BigDecimal("114.35"));
        inspection.setLocation("长江武汉段采砂点A区");
        inspection.setActualVolume(new BigDecimal("1000"));
        inspection.setPhotoPlaceholders("photo_1.jpg,photo_2.jpg,photo_3.jpg");
        inspection.setResult(InspectionResult.NORMAL);
        inspection.setRemark("现场核查正常，采砂量未超许可。");
        inspection.setCreateTime(LocalDateTime.now());
        inspectionRepository.save(inspection);
    }

    private void createDemoPenaltyPath(MiningApplicationRepository applicationRepository,
                                       MiningPermitRepository permitRepository,
                                       OnsiteInspectionRepository inspectionRepository,
                                       PenaltyClueRepository penaltyClueRepository,
                                       RiverSection section, Vessel vessel,
                                       User enterprise, User auditor,
                                       User enforcer, User penaltyHandler) {
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = startDate.plusMonths(2);

        MiningApplication app = new MiningApplication();
        app.setApplicationNo("SQDEMO002");
        app.setApplicantId(enterprise.getId());
        app.setEnterpriseName(enterprise.getEnterpriseName());
        app.setRiverSectionId(section.getId());
        app.setRiverSectionName(section.getSectionName());
        app.setVesselIds(vessel.getId().toString());
        app.setVesselNames(vessel.getVesselName());
        app.setStartDate(startDate);
        app.setEndDate(endDate);
        app.setEstimatedVolume(new BigDecimal("2000"));
        app.setDepositAmount(new BigDecimal("20000"));
        app.setStatus(ApplicationStatus.APPROVED);
        app.setRemark("演示数据-超量处罚路径");
        app.setReviewOpinion("审核通过，请严格按照许可量开采。");
        app.setReviewerId(auditor.getId());
        app.setReviewTime(LocalDateTime.now());
        app.setCreateBy(enterprise.getUsername());
        app.setCreateTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());
        applicationRepository.save(app);

        MiningPermit permit = new MiningPermit();
        permit.setPermitNo("XKDEMO002");
        permit.setApplicationId(app.getId());
        permit.setHolderId(enterprise.getId());
        permit.setHolderName(enterprise.getEnterpriseName());
        permit.setRiverSectionId(section.getId());
        permit.setRiverSectionName(section.getSectionName());
        permit.setVesselIds(vessel.getId().toString());
        permit.setVesselNames(vessel.getVesselName());
        permit.setStartDate(startDate);
        permit.setEndDate(endDate);
        permit.setPermittedVolume(new BigDecimal("2000"));
        permit.setUsedVolume(new BigDecimal("1500"));
        permit.setRemainingVolume(new BigDecimal("500"));
        permit.setDepositAmount(new BigDecimal("20000"));
        permit.setStatus(PermitStatus.ACTIVE);
        permit.setIssueDate(LocalDateTime.now());
        permit.setUpdateTime(LocalDateTime.now());
        permitRepository.save(permit);

        OnsiteInspection inspection = new OnsiteInspection();
        inspection.setPermitId(permit.getId());
        inspection.setPermitNo(permit.getPermitNo());
        inspection.setInspectorId(enforcer.getId());
        inspection.setInspectorName(enforcer.getRealName());
        inspection.setInspectionTime(LocalDateTime.now());
        inspection.setLatitude(new BigDecimal("34.75"));
        inspection.setLongitude(new BigDecimal("113.68"));
        inspection.setLocation("黄河郑州段采砂点B区");
        inspection.setActualVolume(new BigDecimal("1000"));
        inspection.setPhotoPlaceholders("photo_a.jpg,photo_b.jpg");
        inspection.setResult(InspectionResult.ABNORMAL);
        inspection.setAbnormalDescription("超许可方量开采，超采量：500.0方");
        inspection.setRemark("现场核查发现超采，已生成处罚线索。");
        inspection.setCreateTime(LocalDateTime.now());
        inspectionRepository.save(inspection);

        permit.setUsedVolume(new BigDecimal("2500"));
        permit.setRemainingVolume(BigDecimal.ZERO);
        permitRepository.save(permit);

        PenaltyClue clue = new PenaltyClue();
        clue.setClueNo("XZDEMO001");
        clue.setClueType("超许可方量");
        clue.setClueSource("现场核查");
        clue.setPermitId(permit.getId());
        clue.setPermitNo(permit.getPermitNo());
        clue.setInspectionId(inspection.getId());
        clue.setEnterpriseId(enterprise.getId());
        clue.setEnterpriseName(enterprise.getEnterpriseName());
        clue.setDescription("现场核查发现超许可方量开采，许可量：2000方，累计采量：2500方，超采量：500方");
        clue.setExceedVolume(new BigDecimal("500"));
        clue.setPenaltyAmount(new BigDecimal("25000"));
        clue.setStatus(com.river.sand.enums.PenaltyStatus.CONFIRMED);
        clue.setReviewOpinion("审核通过，情况属实，按规定处罚。");
        clue.setReviewerId(auditor.getId());
        clue.setReviewTime(LocalDateTime.now());
        clue.setHandlerId(penaltyHandler.getId());
        clue.setHandleResult("已处罚，罚款25000元，企业已缴纳。");
        clue.setHandleTime(LocalDateTime.now());
        clue.setCreateTime(LocalDateTime.now());
        clue.setUpdateTime(LocalDateTime.now());
        penaltyClueRepository.save(clue);
    }
}
