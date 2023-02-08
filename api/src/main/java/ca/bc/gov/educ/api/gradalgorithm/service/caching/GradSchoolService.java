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
	private final ReadWriteLock minCodeSchoolMapLock = new ReentrantReadWriteLock();
	private Map<String, School> mincodeSchoolEntityMap;

	/**
	 * Search for SchoolEntity by Mincode
	 *
	 * @param mincode the unique mincode for a given school.
	 * @return the School entity if found.
	 */
	public School retrieveSchoolByMincode(String mincode) {
		Optional<School> result = Optional.ofNullable(this.mincodeSchoolEntityMap.get(mincode));
		return result.orElse(null);
	}

	/**
	 * Init.
	 */
	@PostConstruct
	public void init() {
		ResponseObj obj = getTokenResponseObject();
		this.setSchoolData(obj.getAccess_token());
		logger.info("loaded school cache..");
	}

	private void setSchoolData(String accessToken) {
		val writeLock = minCodeSchoolMapLock.writeLock();
		try {
			writeLock.lock();
			List<School> schoolList = webClient.get()
					.uri(constants.getAllSchools())
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					})
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<School>>(){}).block();
			if(schoolList != null)
				this.mincodeSchoolEntityMap = schoolList.stream().collect(Collectors.toConcurrentMap(School::getMinCode, Function.identity()));
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Reload cache at midnight
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void reloadCache() {
		logger.info("started reloading cache..");
		ResponseObj obj = getTokenResponseObject();
		this.setSchoolData(obj.getAccess_token());
		logger.info("reloading cache completed..");
	}
}
