package ca.bc.gov.educ.api.gradalgorithm.mapper.v1;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradSchool;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.District;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.SchoolCategoryCode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SchoolLookups {
    final Map<String, District> districtById;
    final Map<String, SchoolCategoryCode> categoryByCode;
    final Map<String, GradSchool> gradBySchoolId;

    private SchoolLookups(Map<String, District> districtById,
                          Map<String, SchoolCategoryCode> categoryByCode,
                          Map<String, GradSchool> gradBySchoolId) {
        this.districtById = districtById;
        this.categoryByCode = categoryByCode;
        this.gradBySchoolId = gradBySchoolId;
    }

    public static SchoolLookups of(List<District> districts,
                                   List<SchoolCategoryCode> cats,
                                   List<GradSchool> grads) {
        Map<String, District> d = districts == null ? Map.of() :
                districts.stream().filter(Objects::nonNull)
                        .collect(Collectors.toUnmodifiableMap(District::getDistrictId, Function.identity(), (a, b)->a));
        Map<String, SchoolCategoryCode> c = cats == null ? Map.of() :
                cats.stream().filter(Objects::nonNull)
                        .collect(Collectors.toUnmodifiableMap(SchoolCategoryCode::getSchoolCategoryCode, Function.identity(), (a,b)->a));
        Map<String, GradSchool> g = grads == null ? Map.of() :
                grads.stream().filter(Objects::nonNull)
                        .collect(Collectors.toUnmodifiableMap(GradSchool::getSchoolID, Function.identity(), (a,b)->a));
        return new SchoolLookups(d, c, g);
    }
}
