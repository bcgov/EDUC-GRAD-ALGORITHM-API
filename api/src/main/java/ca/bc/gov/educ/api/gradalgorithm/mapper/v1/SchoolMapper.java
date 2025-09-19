package ca.bc.gov.educ.api.gradalgorithm.mapper.v1;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradSchool;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.District;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.SchoolCategoryCode;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.SchoolDetail;
import org.mapstruct.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {StringUtils.class}, uses = SchoolMapperHelpers.class)
public interface SchoolMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(source = "mincode", target = "minCode"),
            @Mapping(source = "schoolId", target = "schoolId"),
            @Mapping(target = "schoolName", expression = "java((StringUtils.isBlank(source.getDisplayNameNoSpecialChars()) ? source.getDisplayName() : source.getDisplayNameNoSpecialChars()))"),
            @Mapping(source = "districtId", target = "districtId"),
            @Mapping(source = "addresses", target = "address1", qualifiedByName = "firstAddressLine1"),
            @Mapping(source = "addresses", target = "address2", qualifiedByName = "firstAddressLine2"),
            @Mapping(source = "addresses", target = "city", qualifiedByName = "firstCity"),
            @Mapping(source = "addresses", target = "provCode", qualifiedByName = "firstProvCode"),
            @Mapping(source = "addresses", target = "countryCode", qualifiedByName = "firstCountryCode"),
            @Mapping(source = "addresses", target = "postal", qualifiedByName = "firstPostalCode"),
            @Mapping(source = "closedDate", target = "openFlag", qualifiedByName = "openFlag"),
            @Mapping(source = "schoolCategoryCode", target = "schoolCategoryCode")
    })
    School toSchool(SchoolDetail source, @Context SchoolLookups lookups);

    default Map<String, School> toSchoolMapById(List<SchoolDetail> schoolDetails, List<District> districts,
                                                List<SchoolCategoryCode> categories,
                                                List<GradSchool> grads) {
        if (schoolDetails == null) return Collections.emptyMap();
        SchoolLookups lookups = SchoolLookups.of(districts, categories, grads);

        return schoolDetails.stream()
                .filter(Objects::nonNull)
                .map(ts -> this.toSchool(ts, lookups))
                .filter(s -> s != null && StringUtils.isNotBlank(s.getSchoolId()))
                .collect(Collectors.toMap(
                        School::getSchoolId,
                        Function.identity(),
                        (first, dup) -> dup,          // choose your merge policy (dup wins here)
                        ConcurrentHashMap::new
                ));
    }

    @AfterMapping
    default void overlay(@MappingTarget School target,
                        SchoolDetail source,
                        @Context SchoolLookups lookups) {

        // District name
        if (StringUtils.isNotBlank(target.getDistrictId())) {
            District d = lookups.districtById.get(target.getDistrictId());
            if (d != null) {
                target.setDistrictName(d.getDisplayName());
            }
        }
        // Legacy category code
        if (StringUtils.isNotBlank(target.getSchoolCategoryCode())) {
            SchoolCategoryCode cat = lookups.categoryByCode.get(target.getSchoolCategoryCode());
            if (cat != null) {
                target.setSchoolCategoryLegacyCode(cat.getLegacyCode()); // adjust getter name as needed
            }
        }
        // Cert and transcript eligibility
        if (StringUtils.isNotBlank(target.getSchoolId())) {
            GradSchool gs = lookups.gradBySchoolId.get(target.getSchoolId());
            if (gs != null) {
                target.setCertificateEligibility(gs.getCanIssueCertificates());
                target.setTranscriptEligibility(gs.getCanIssueTranscripts());
            }
        }
    }
}
