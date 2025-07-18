package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

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

@Slf4j
@Service
public class GradProgramService extends GradService {

    private final ReadWriteLock programMapLock = new ReentrantReadWriteLock();
	private Map<String, GradProgramAlgorithmData> programAlgorithmDataMap;

	@Autowired
	public GradProgramService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService) {
		super(constants, algorithmApiClient, restService);
	}

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
		this.setProgramAlgorithmData();
		log.info("loaded program cache..");
	}
	private void setProgramAlgorithmData() {
		val writeLock = programMapLock.writeLock();
		try {
			writeLock.lock();
			List<GradProgramAlgorithmData> data = restService.get(constants.getProgramData(),
					new ParameterizedTypeReference<List<GradProgramAlgorithmData>>() {}, algorithmApiClient);
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
		log.debug("started reloading cache..");
		this.setProgramAlgorithmData();
		log.debug("reloading cache completed..");
	}
}
