package ca.bc.gov.educ.api.gradalgorithm.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Data
@Component
public class ProgramSets {
    private List<UUID> programSetIDs;
}
