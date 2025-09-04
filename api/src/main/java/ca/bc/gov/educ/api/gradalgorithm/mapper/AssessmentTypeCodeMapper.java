package ca.bc.gov.educ.api.gradalgorithm.mapper;

import ca.bc.gov.educ.api.gradalgorithm.dto.Assessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.AssessmentTypeCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {DateStringMapper.class})
public interface AssessmentTypeCodeMapper {

    AssessmentTypeCodeMapper mapper = Mappers.getMapper(AssessmentTypeCodeMapper.class);

    @Mapping(target = "assessmentCode", source = "assessmentTypeCode")
    @Mapping(target = "assessmentName", source = "label")
    @Mapping(target = "language", source = "language")
    @Mapping(target = "startDate", source = "effectiveDate")
    @Mapping(target = "endDate", source = "expiryDate")
    Assessment toAssessment(AssessmentTypeCode assessmentTypeCode);

    List<Assessment> toAssessmentList(List<AssessmentTypeCode> assessmentTypeCodes);
}
