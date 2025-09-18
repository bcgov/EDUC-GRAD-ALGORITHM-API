package ca.bc.gov.educ.api.gradalgorithm.mapper.v1;

import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.dto.SchoolTombstone;
import org.mapstruct.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {StringUtils.class}, uses = MapperHelpers.class)
public interface SchoolMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(source = "mincode", target = "minCode"),
            @Mapping(source = "schoolId", target = "schoolId"),
            @Mapping(target = "schoolName", expression = "java((StringUtils.isBlank(source.getDisplayNameNoSpecialChars()) ? source.getDisplayName() : source.getDisplayNameNoSpecialChars()))"),
            @Mapping(source = "districtId", target = "districtId"),
            @Mapping(source = "canIssueTranscripts", target = "transcriptEligibility", qualifiedByName = "boolToYN"),
            @Mapping(source = "canIssueCertificates", target = "certificateEligibility", qualifiedByName = "boolToYN"),
            // openFlag: Y if no closedDate, else N
            @Mapping(target = "openFlag", expression = "java((StringUtils.isBlank(source.getClosedDate()) ? \"Y\" : \"N\"))"),
            @Mapping(source = "schoolCategoryCode", target = "schoolCategoryCode")
    })
    School toSchool(SchoolTombstone source);

    default Map<String, School> toSchoolMapById(List<SchoolTombstone> sources) {
        if (sources == null) return Collections.emptyMap();

        return sources.stream()
                .map(this::toSchool)
                .filter(s -> s != null && StringUtils.isNotBlank(s.getSchoolId()))
                .collect(Collectors.toMap(
                        School::getSchoolId,
                        Function.identity(),
                        (first, dup) -> first,
                        ConcurrentHashMap::new
                ));
    }
}
