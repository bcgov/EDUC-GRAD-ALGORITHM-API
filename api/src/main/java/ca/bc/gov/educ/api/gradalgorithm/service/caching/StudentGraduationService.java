package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.service.GradService;
import ca.bc.gov.educ.api.gradalgorithm.service.RESTService;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StudentGraduationService extends GradService {

    private final ReadWriteLock programStudentGraduationMapLock = new ReentrantReadWriteLock();
	private Map<String, StudentGraduationAlgorithmData> programStudentGraduationAlgorithmDataMap;

	@Autowired
	public StudentGraduationService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService) {
		super(constants, algorithmApiClient, restService);
	}

	/**
	 * Search for StudentGraduationAlgorithmData by program code
	 *
	 * @param programCode the unique program.
	 * @return the School entity if found.
	 */
	public StudentGraduationAlgorithmData retrieveStudentGraduationDataByProgramCode(String programCode) {
		Optional<StudentGraduationAlgorithmData> result = Optional.ofNullable(this.programStudentGraduationAlgorithmDataMap.get(programCode));
		return result.orElse(null);
	}

	/**
	 * Init.
	 */
	@PostConstruct
	public void init() {
		this.setStudentGraduationAlgorithmData();
		log.debug("loaded student graduation cache..");
	}

	private void setStudentGraduationAlgorithmData() {
		val writeLock = programStudentGraduationMapLock.writeLock();
		try {
			writeLock.lock();
			List<StudentGraduationAlgorithmData> data = restService.get(constants.getStudentGraduationAlgorithmURL() + "/algorithmdata",
					new ParameterizedTypeReference<List<StudentGraduationAlgorithmData>>(){}, algorithmApiClient);

			if(data != null)
				this.programStudentGraduationAlgorithmDataMap = data.stream().collect(Collectors.toConcurrentMap(StudentGraduationAlgorithmData::getGradProgram, Function.identity()));
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Reload cache at midnight
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void reloadStudentGraduationCache() {
		log.debug("started reloading cache..");
		this.setStudentGraduationAlgorithmData();
		log.debug("reloading cache completed..");
	}
    
}
