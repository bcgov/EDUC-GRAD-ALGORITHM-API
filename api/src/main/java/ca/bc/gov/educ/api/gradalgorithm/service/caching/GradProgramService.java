package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.service.GradService;
import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

import jakarta.annotation.PostConstruct;

@Service
public class GradProgramService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradProgramService.class);
	private final ReadWriteLock programMapLock = new ReentrantReadWriteLock();
	private Map<String, GradProgramAlgorithmData> programAlgorithmDataMap;

	/**
	 * Search for StudentGraduationAlgorithmData by program code
	 *
	 * @param programCode the unique program.
	 * @return the School entity if found.
	 */
	public GradProgramAlgorithmData retrieveProgramDataByProgramCode(String programCode,String optionalProgramCode) {
		Optional<GradProgramAlgorithmData> result = Optional.ofNullable(this.programAlgorithmDataMap.get(programCode+" "+optionalProgramCode));
		return result.orElse(null);
	}

	/**
	 * Init.
	 */
	@PostConstruct
	public void init() {
		ResponseObj obj = getTokenResponseObject();
		this.setProgramAlgorithmData(obj.getAccess_token());
		logger.info("loaded program cache..");
	}
	private void setProgramAlgorithmData(String accessToken) {
		val writeLock = programMapLock.writeLock();
		try {
			writeLock.lock();
			List<GradProgramAlgorithmData> data = webClient.get()
					.uri(constants.getProgramData())
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					})
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<GradProgramAlgorithmData>>(){})
					.block();
			if(data != null)
				this.programAlgorithmDataMap = data.stream().collect(Collectors.toConcurrentMap(GradProgramAlgorithmData::getProgramKey, Function.identity()));
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Reload cache at midnight
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void reloadStudentGraduationCache() {
		logger.debug("started reloading cache..");
		ResponseObj obj = getTokenResponseObject();
		this.setProgramAlgorithmData(obj.getAccess_token());
		logger.debug("reloading cache completed..");
	}
}
