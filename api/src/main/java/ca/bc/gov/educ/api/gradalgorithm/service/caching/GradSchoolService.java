package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import ca.bc.gov.educ.api.gradalgorithm.dto.ResponseObj;
import ca.bc.gov.educ.api.gradalgorithm.service.GradService;
import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GradSchoolService extends GradService {

	private static final Logger logger = LoggerFactory.getLogger(GradSchoolService.class);
	private final ReadWriteLock schoolClobMapLock = new ReentrantReadWriteLock();
	private Map<String, School> schoolClobMap;

	/**
	 * Search for SchoolEntity by SchoolId
	 */
	public School retrieveSchoolBySchoolId(String schoolId) {
		Optional<School> result = Optional.ofNullable(this.schoolClobMap.get(schoolId));
		return result.orElse(null);
	}

	/**
	 * Init.
	 */
	@PostConstruct
	public void init() {
		ResponseObj obj = getTokenResponseObject();
		this.setSchoolData(obj.getAccess_token());
		logger.debug("loaded school cache..");
	}

	private void setSchoolData(String accessToken) {
		val writeLock = schoolClobMapLock.writeLock();
		try {
			writeLock.lock();
			List<School> schoolList = webClient.get()
					.uri(constants.getAllSchools())
					.headers(h -> h.setBearerAuth(accessToken))
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<School>>(){}).block();
			if(schoolList != null)
				this.schoolClobMap = schoolList.stream().collect(Collectors.toConcurrentMap(School::getSchoolId, Function.identity()));
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Reload cache at midnight
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void reloadCache() {
		logger.debug("started reloading cache..");
		ResponseObj obj = getTokenResponseObject();
		this.setSchoolData(obj.getAccess_token());
		logger.debug("reloading cache completed..");
	}
}
