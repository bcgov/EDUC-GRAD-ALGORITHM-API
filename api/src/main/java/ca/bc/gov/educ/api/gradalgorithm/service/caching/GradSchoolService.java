package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradSchool;
import ca.bc.gov.educ.api.gradalgorithm.dto.SchoolTombstone;
import ca.bc.gov.educ.api.gradalgorithm.mapper.v1.SchoolMapper;
import ca.bc.gov.educ.api.gradalgorithm.service.GradService;
import ca.bc.gov.educ.api.gradalgorithm.service.RESTService;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import jakarta.annotation.PostConstruct;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class GradSchoolService extends GradService {

	private final ReadWriteLock schoolMapLock = new ReentrantReadWriteLock();
	private final SchoolMapper schoolMapper;
	private Map<String, School> schoolMap;

	@Autowired
	public GradSchoolService(GradAlgorithmAPIConstants constants, WebClient algorithmApiClient, RESTService restService, SchoolMapper schoolMapper) {
		super(constants, algorithmApiClient, restService);
		this.schoolMapper = schoolMapper;
	}

	/**
	 * Search for SchoolEntity by SchoolId
	 */
	public School retrieveSchoolBySchoolId(String schoolId) {
		Optional<School> result = Optional.ofNullable(this.schoolMap.get(schoolId));
		return result.orElse(null);
	}

	/**
	 * Init.
	 */
	@PostConstruct
	public void init() {
		log.debug("Loading school cache..");
		this.setSchoolData();
		log.debug("loaded school cache..");
	}

	private void setSchoolData() {
		val writeLock = schoolMapLock.writeLock();
		writeLock.lock();
		try {
			// get schools from institute
			var instituteResponse = this.restService.get(constants.getAllSchoolsFromInstituteApiUrl(),
					SchoolTombstone[].class, null);

			schoolMap = schoolMapper.toSchoolMapById(List.of(instituteResponse));
			// get grad schools
			getSchoolGradDetailsFromSchoolApi().forEach(school -> {
				if(schoolMap.get(school.getSchoolID()) != null){
					schoolMap.get(school.getSchoolID()).setCertificateEligibility(school.getCanIssueCertificates());
					schoolMap.get(school.getSchoolID()).setTranscriptEligibility(school.getCanIssueTranscripts());
				}
			});
		} catch (Exception e) {
			log.error("Error while getting school data.", e);
		}
		finally {
			writeLock.unlock();
		}
	}

	public List<GradSchool> getSchoolGradDetailsFromSchoolApi() {
		try {
			log.trace("****Before Calling Grad School API for schools");
			return Arrays.stream(this.restService.get(constants.getSchoolGradDetailsFromGradSchoolApiUrl(),
					GradSchool[].class, null)).toList();
		} catch (WebClientResponseException e) {
			log.warn("Error getting grad details from Grad School api : {}", e.getMessage());
		} catch (Exception e) {
			log.error("Error getting grad details from Grad School api : {}", e.getMessage());
		}
		return Collections.emptyList();
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
