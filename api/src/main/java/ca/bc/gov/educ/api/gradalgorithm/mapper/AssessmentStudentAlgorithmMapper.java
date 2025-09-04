package ca.bc.gov.educ.api.gradalgorithm.mapper;

import ca.bc.gov.educ.api.gradalgorithm.dto.StudentAssessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.AssessmentStudentListItem;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@DecoratedWith(AssessmentStudentAlgorithmMapperDecorator.class)
public interface AssessmentStudentAlgorithmMapper {

    AssessmentStudentAlgorithmMapper mapper = Mappers.getMapper(AssessmentStudentAlgorithmMapper.class);

    @Mapping(target = "assessmentCode", source = "assessmentTypeCode")
    @Mapping(target = "specialCase", source = "provincialSpecialCaseCode")
    StudentAssessment toAlgorithmData(AssessmentStudentListItem assessmentStudent);
}
