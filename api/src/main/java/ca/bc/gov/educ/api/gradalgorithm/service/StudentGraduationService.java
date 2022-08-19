package ca.bc.gov.educ.api.gradalgorithm.service;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
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
		if (result.isPresent()) {
			return result.get();
		} else {
			return null;
		}
	}

	/**
	 * Init.
	 */
	@PostConstruct
	public void init() {

		ResponseObj obj = getTokenResponseObject();
		this.setStudentGraduationAlgorithmData("2018-EN",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("2018-PF",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("2004-EN",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("2004-PF",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("1996-EN",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("1996-PF",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("1986-EN",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("1950",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("SCCP",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("NOPROG",obj.getAccess_token());
		logger.info("loaded student graduation cache..");
	}

	private void setStudentGraduationAlgorithmData(String programCode,String accessToken) {
		val writeLock = programStudentGraduationMapLock.writeLock();
		try {
			writeLock.lock();
			StudentGraduationAlgorithmData data = webClient.get()
					.uri(constants.getStudentGraduationAlgorithmURL() + "/algorithmdata/"+programCode)
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					})
					.retrieve()
					.bodyToMono(StudentGraduationAlgorithmData.class).block();

			if(this.programStudentGraduationAlgorithmDataMap == null) {
				this.programStudentGraduationAlgorithmDataMap = new HashMap<>();
			}
			this.programStudentGraduationAlgorithmDataMap.put(programCode,data);
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
		this.setStudentGraduationAlgorithmData("2018-EN",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("2018-PF",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("2004-EN",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("2004-PF",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("1996-EN",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("1996-PF",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("1986-EN",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("1950",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("SCCP",obj.getAccess_token());
		this.setStudentGraduationAlgorithmData("NOPROG",obj.getAccess_token());
		logger.info("reloading cache completed..");
	}
    
}
