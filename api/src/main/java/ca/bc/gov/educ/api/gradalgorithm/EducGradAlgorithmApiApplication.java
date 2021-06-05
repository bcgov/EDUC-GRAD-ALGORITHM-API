package ca.bc.gov.educ.api.gradalgorithm;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableCaching
public class EducGradAlgorithmApiApplication {

	private static Logger logger = LoggerFactory.getLogger(EducGradAlgorithmApiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(EducGradAlgorithmApiApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public WebClient webClient() {
		HttpClient client = HttpClient.create();
		client.warmup().block();
		logger.debug("$$$$$ Initializing Web Client");
		return WebClient.builder().build();
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
