package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import ca.bc.gov.educ.api.gradalgorithm.dto.Assessment;
import ca.bc.gov.educ.api.gradalgorithm.dto.v2.AssessmentTypeCode;
import ca.bc.gov.educ.api.gradalgorithm.mapper.AssessmentTypeCodeMapper;
import ca.bc.gov.educ.api.gradalgorithm.service.GradService;
import ca.bc.gov.educ.api.gradalgorithm.service.RESTService;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class StudentAssessmentCacheService extends GradService {

	private static final AssessmentTypeCodeMapper mapper = AssessmentTypeCodeMapper.mapper;
	private final ReadWriteLock assessmentMapLock = new ReentrantReadWriteLock();
	private final Map<String, AssessmentTypeCode> assessmentTypeCodesMap = new ConcurrentHashMap<>();

	@Autowired
	public StudentAssessmentCacheService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService) {
		super(constants, algorithmApiClient, restService);
	}

	public List<AssessmentTypeCode> getAllAssessmentTypeCodes() {
		if(this.assessmentTypeCodesMap.isEmpty()) {
			log.info("AssessmentTypeCodeMap is empty, reloading");
			this.setAssessmentTypeCodesMap();
		}
		return new ArrayList<>(this.assessmentTypeCodesMap.values());
	}

	public String getAssessmentNameByCode(String code) {
		if(this.assessmentTypeCodesMap.isEmpty()) {
			log.info("AssessmentTypeCodeMap is empty, reloading");
			this.setAssessmentTypeCodesMap();
		}
		return Optional.ofNullable(assessmentTypeCodesMap.get(code))
				.map(AssessmentTypeCode::getLabel)
				.orElse(null);
	}

	public List<Assessment> getAllAssessments() {
		List<AssessmentTypeCode> assessmentTypeCodes = this.getAllAssessmentTypeCodes();
		return mapper.toAssessmentList(assessmentTypeCodes);
	}

	@PostConstruct
	public void init() {
		log.info("loading assessment type codes..");
		this.setAssessmentTypeCodesMap();
	}
	private void setAssessmentTypeCodesMap() {
		val writeLock = this.assessmentMapLock.writeLock();
		try {
			writeLock.lock();
			List<AssessmentTypeCode> codes = this.restService.get(this.constants.getAssessmentTypeCodes(), new ParameterizedTypeReference<List<AssessmentTypeCode>>() {}, this.algorithmApiClient);

			for (val code : codes) {
				this.assessmentTypeCodesMap.put(code.getAssessmentTypeCode(), code);
			}
		} catch (Exception ex) {
      log.error("Unable to load map cache assessment types {}", String.valueOf(ex));
		} finally {
			writeLock.unlock();
			log.info("Loaded  {} assessment types to memory", this.assessmentTypeCodesMap.size());
		}
	}

	/**
	 * Reload cache at midnight
	 */
	@Scheduled(cron = "0 0 0 * * *")
	public void reloadStudentGraduationCache() {
		log.debug("started reloading assessment cache..");
		this.setAssessmentTypeCodesMap();
	}
}
