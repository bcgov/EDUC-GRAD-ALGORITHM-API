package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import ca.bc.gov.educ.api.gradalgorithm.service.GradService;
import ca.bc.gov.educ.api.gradalgorithm.service.RESTService;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
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
public class GradSchoolService extends GradService {

	private final ReadWriteLock schoolClobMapLock = new ReentrantReadWriteLock();
	private Map<String, School> schoolClobMap;

	public GradSchoolService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService) {
		super(constants, algorithmApiClient, restService);
	}

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
		this.setSchoolData();
		log.debug("loaded school cache..");
	}

	private void setSchoolData() {
		val writeLock = schoolClobMapLock.writeLock();
		try {
			writeLock.lock();
			List<School> schoolList = restService.get(constants.getAllSchools(), new ParameterizedTypeReference<List<School>>(){},
					algorithmApiClient);
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
		log.debug("started reloading cache..");
		this.setSchoolData();
		log.debug("reloading cache completed..");
	}
}
