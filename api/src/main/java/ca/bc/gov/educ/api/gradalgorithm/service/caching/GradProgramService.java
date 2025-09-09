package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
	private final ReadWriteLock assessmentMapLock = new ReentrantReadWriteLock();
	private Map<String, GradProgramAlgorithmData> programAlgorithmDataMap;
	private final Map<UUID, AssessmentRequirement>  assessmentRequirementMap = new ConcurrentHashMap<>();

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

	public List<AssessmentRequirement> getAllAssessmentRequirements() {
		if(this.assessmentRequirementMap.isEmpty()) {
			log.info("AssessmentTypeCodeMap is empty, reloading");
			this.setAssessmentRequirementsData();
		}
		return new ArrayList<>(this.assessmentRequirementMap.values());
	}

	/**
	 * Init.
	 */
	@PostConstruct
	public void init() {
		this.setProgramAlgorithmData();
		log.info("loaded program cache..");
		log.info("loading assessment requirements..");
		this.setAssessmentRequirementsData();
	}
	private void setProgramAlgorithmData() {
		val writeLock = programMapLock.writeLock();
		try {
			writeLock.lock();
			List<GradProgramAlgorithmData> data = restService.get(constants.getProgramData(),
          new ParameterizedTypeReference<>() {}, algorithmApiClient);
			if(data != null)
				this.programAlgorithmDataMap = data.stream().collect(Collectors.toConcurrentMap(GradProgramAlgorithmData::getProgramKey, Function.identity()));
		} finally {
			writeLock.unlock();
		}
	}

	private void setAssessmentRequirementsData() {
		val writeLock = this.assessmentMapLock.writeLock();
		try {
			writeLock.lock();
			List<AssessmentRequirement> codes = this.restService.get(this.constants.getAssessmentRequirements(), new ParameterizedTypeReference<>() {}, this.algorithmApiClient);

			for (val code : codes) {
				this.assessmentRequirementMap.put(code.getAssessmentRequirementId(), code);
			}
		} catch (Exception ex) {
			log.error("Unable to load map cache assessment requirements {}", String.valueOf(ex));
		} finally {
			writeLock.unlock();
			log.info("Loaded  {} assessment requirements to memory", this.assessmentRequirementMap.size());
		}
	}

	/**
	 * Reload cache at midnight
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void reloadStudentGraduationCache() {
		log.debug("started reloading cache..");
		this.setProgramAlgorithmData();
		this.setAssessmentRequirementsData();
		log.debug("reloading cache completed..");
	}
}
