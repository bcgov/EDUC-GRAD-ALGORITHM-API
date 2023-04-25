package ca.bc.gov.educ.api.gradalgorithm.config;


import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;

import static ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants.DATETIME_FORMAT;

@Configuration
public class GradAlgorithmApiConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
        return jacksonObjectMapperBuilder ->
                jacksonObjectMapperBuilder
                        .serializers(localDateTimeSerializer);
    }
}
