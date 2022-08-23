package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.service.GradService;
import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StudentGraduationService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(StudentGraduationService.class);

	private final ReadWriteLock programStudentGraduationMapLock = new ReentrantReadWriteLock();
	private Map<String, StudentGraduationAlgorithmData> programStudentGraduationAlgorithmDataMap;

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
		ResponseObj obj = getTokenResponseObject();
		this.setStudentGraduationAlgorithmData(obj.getAccess_token());
		logger.info("loaded student graduation cache..");
	}

	private void setStudentGraduationAlgorithmData(String accessToken) {
		val writeLock = programStudentGraduationMapLock.writeLock();
		try {
			writeLock.lock();
			List<StudentGraduationAlgorithmData> data = webClient.get()
					.uri(constants.getStudentGraduationAlgorithmURL() + "/algorithmdata")
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					})
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<StudentGraduationAlgorithmData>>(){}).block();

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
		logger.info("started reloading cache..");
		ResponseObj obj = getTokenResponseObject();
		this.setStudentGraduationAlgorithmData(obj.getAccess_token());
		logger.info("reloading cache completed..");
	}
    
}
