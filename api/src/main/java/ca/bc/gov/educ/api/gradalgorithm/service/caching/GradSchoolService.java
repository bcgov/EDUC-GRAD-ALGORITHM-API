package ca.bc.gov.educ.api.gradalgorithm.service.caching;

import ca.bc.gov.educ.api.gradalgorithm.dto.GradSchool;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.District;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.PaginatedResponse;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.SchoolCategoryCode;
import ca.bc.gov.educ.api.gradalgorithm.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.gradalgorithm.mapper.v1.SchoolMapper;
import ca.bc.gov.educ.api.gradalgorithm.service.GradService;
import ca.bc.gov.educ.api.gradalgorithm.service.RESTService;
import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import jakarta.annotation.PostConstruct;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class GradSchoolService extends GradService {
	
	private static final String INSTITUTE = "institute-api";
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
			List<SchoolDetail> schoolDetails = getSchoolDetailsFromInstituteApi();
			// get grad school details
			List<GradSchool> gradSchoolList = getSchoolGradDetailsFromSchoolApi();
			// get district info
			List<District> districts = getDistrictsFromInstituteApi();
			// get category codes
			List<SchoolCategoryCode> schoolCategoryCodes = getSchoolCategoryCodesFromInstituteApi();
			// map it all together
			schoolMap = schoolMapper.toSchoolMapById(schoolDetails, districts, schoolCategoryCodes,  gradSchoolList);

		} catch (Exception e) {
			log.error("Error while getting school data.", e);
		}
		finally {
			writeLock.unlock();
		}
	}

	private List<SchoolCategoryCode> getSchoolCategoryCodesFromInstituteApi() {
		log.trace("****Before Calling Institute API for category codes *****");
		try {
			return Arrays.stream(this.restService.get(constants.getAllCategoryCodesFromInstituteApiUrl(),
					SchoolCategoryCode[].class, null)).toList();
		} catch (WebClientResponseException e) {
			log.warn("Error getting school category codes from Institute Api : {}", e.getMessage());
		} catch (Exception e) {
			generateGenericCalloutError(INSTITUTE, e);
		}
		return Collections.emptyList();
	}

	private List<District> getDistrictsFromInstituteApi() {
		log.trace("****Before Calling Institute API for district details");
		try {
			return Arrays.stream(this.restService.get(constants.getAllDistrictsFromInstituteApiUrl(),
					District[].class, null)).toList();
		} catch (WebClientResponseException e) {
			log.warn("Error getting district details from Institute Api : {}", e.getMessage());
		} catch (Exception e) {
			generateGenericCalloutError(INSTITUTE, e);
		}
		return Collections.emptyList();
	}

	private List<SchoolDetail> getSchoolDetailsFromInstituteApi() {
		log.trace("****Before Calling Institute API for school details");
		return getSchoolDetailsPaginatedFromInstituteApi(0, new ArrayList<>());
	}

	private List<SchoolDetail> getSchoolDetailsPaginatedFromInstituteApi(int pageNumber, List<SchoolDetail> schoolDetails) {
		PaginatedResponse<SchoolDetail> response =  getSchoolDetailsPaginatedFromInstituteApi(pageNumber);
		if (response == null) {
			return schoolDetails;
		}
		List<SchoolDetail> pagedSchoolDetails = response.getContent();
		if(!CollectionUtils.isEmpty(pagedSchoolDetails)) {
			schoolDetails.addAll(pagedSchoolDetails);
		}
		if (response.hasNext()) {
			return getSchoolDetailsPaginatedFromInstituteApi(response.nextPageable().getPageNumber(), schoolDetails);
		}
		return schoolDetails;
	}

	private PaginatedResponse<SchoolDetail> getSchoolDetailsPaginatedFromInstituteApi(int pageNumber) {
		int pageSize = 1000;
		try {
			return this.restService.get(
			String.format("%s?pageNumber=%d&pageSize=%d", constants.getSchoolsPaginatedFromInstituteApiUrl(), pageNumber, pageSize),
					new ParameterizedTypeReference<PaginatedResponse<SchoolDetail>>() {},
					null);
		} catch (WebClientResponseException e) {
			log.warn("Error getting School Details from Institute API: {}", e.getMessage());
		} catch (Exception e) {
			generateGenericCalloutError(INSTITUTE, e);
		}
		log.warn("No school details found for the given search criteria.");
		return null;
	}

	private List<GradSchool> getSchoolGradDetailsFromSchoolApi() {
		try {
			log.trace("****Before Calling Grad School API for schools");
			return Arrays.stream(this.restService.get(constants.getSchoolGradDetailsFromGradSchoolApiUrl(),
					GradSchool[].class, null)).toList();
		} catch (WebClientResponseException e) {
			log.warn("Error getting grad details from Grad School api : {}", e.getMessage());
		} catch (Exception e) {
			generateGenericCalloutError("grad-school-api", e);
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
	
	private void generateGenericCalloutError(String api, Exception e){
		log.error("Error while calling {} : {}", api, e.getMessage());
	}
}
