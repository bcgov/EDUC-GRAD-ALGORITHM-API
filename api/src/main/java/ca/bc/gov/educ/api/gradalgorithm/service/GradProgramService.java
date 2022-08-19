package ca.bc.gov.educ.api.gradalgorithm.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ca.bc.gov.educ.api.gradalgorithm.dto.*;
import ca.bc.gov.educ.api.gradalgorithm.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradalgorithm.util.GradAlgorithmAPIConstants;

import javax.annotation.PostConstruct;

@Service
public class GradProgramService extends GradService {

    private static final Logger logger = LoggerFactory.getLogger(GradProgramService.class);
	private static final String GRAD_2018_EN = "2018-EN";
	private static final String GRAD_2018_PF = "2018-PF";
	private static final String GRAD_2004_EN = "2004-EN";
	private static final String GRAD_2004_PF = "2004-PF";
	private static final String GRAD_1996_EN = "1996-EN";
	private static final String GRAD_1996_PF = "1996-PF";
	private static final String GRAD_1986_EN = "1986-EN";
	private final ReadWriteLock programMapLock = new ReentrantReadWriteLock();
	private Map<String, GradProgramAlgorithmData> programAlgorithmDataMap;
    
    private static final String EXCEPTION_MESSAGE = "GRAD-PROGRAM-API IS DOWN";

	/**
	 * Search for StudentGraduationAlgorithmData by program code
	 *
	 * @param programCode the unique program.
	 * @return the School entity if found.
	 */
	public GradProgramAlgorithmData retrieveProgramDataByProgramCode(String programCode,String optionalProgramCode) {
		Optional<GradProgramAlgorithmData> result = Optional.ofNullable(this.programAlgorithmDataMap.get(programCode+" "+optionalProgramCode));
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
		this.algData(GRAD_2018_EN,obj.getAccess_token());
		this.algData(GRAD_2018_PF,obj.getAccess_token());
		this.algData(GRAD_2004_EN,obj.getAccess_token());
		this.algData(GRAD_2004_PF,obj.getAccess_token());
		this.algData(GRAD_1996_EN,obj.getAccess_token());
		this.algData(GRAD_1996_PF,obj.getAccess_token());
		this.algData(GRAD_1986_EN,obj.getAccess_token());
		this.algData("1950",obj.getAccess_token());
		this.algData("SCCP",obj.getAccess_token());
		this.algData("NOPROG",obj.getAccess_token());
		logger.info("loaded program cache..");
	}

	private  void algData(String gradProgram,String accessToken) {
		setProgramAlgorithmData(gradProgram,"",accessToken);
		switch (gradProgram) {
			case GRAD_2018_EN:
			case GRAD_1986_EN:
			case GRAD_1996_EN:
			case GRAD_2004_EN:
				setProgramAlgorithmData(gradProgram,"FI",accessToken);
				setProgramAlgorithmData(gradProgram,"CP",accessToken);
				setProgramAlgorithmData(gradProgram,"AD",accessToken);
				setProgramAlgorithmData(gradProgram,"BC",accessToken);
				setProgramAlgorithmData(gradProgram,"BD",accessToken);
				break;
			case GRAD_2018_PF:
			case GRAD_1996_PF:
			case GRAD_2004_PF:
				setProgramAlgorithmData(gradProgram,"DD",accessToken);
				setProgramAlgorithmData(gradProgram,"CP",accessToken);
				setProgramAlgorithmData(gradProgram,"AD",accessToken);
				setProgramAlgorithmData(gradProgram,"BC",accessToken);
				setProgramAlgorithmData(gradProgram,"BD",accessToken);
				break;
			case "1950":
				setProgramAlgorithmData(gradProgram,"CP",accessToken);
				setProgramAlgorithmData(gradProgram,"AD",accessToken);
				setProgramAlgorithmData(gradProgram,"BC",accessToken);
				setProgramAlgorithmData(gradProgram,"BD",accessToken);
				break;
			case "SCCP":
				setProgramAlgorithmData(gradProgram,"FR",accessToken);
				setProgramAlgorithmData(gradProgram,"CP",accessToken);
				break;
			default:
				break;
		}

	}
	private void setProgramAlgorithmData(String programCode,String optionalProgramCode,String accessToken) {
		val writeLock = programMapLock.writeLock();
		try {
			writeLock.lock();
			String url = constants.getProgramData() + "programCode=%s";
			if(StringUtils.isNotBlank(optionalProgramCode)) {
				url = url + "&optionalProgramCode=%s";
			}
			GradProgramAlgorithmData data = webClient.get()
					.uri(String.format(url,programCode,optionalProgramCode))
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					})
					.retrieve()
					.bodyToMono(GradProgramAlgorithmData.class)
					.block();

			if(this.programAlgorithmDataMap == null) {
				this.programAlgorithmDataMap = new HashMap<>();
			}
			this.programAlgorithmDataMap.put(programCode+" "+optionalProgramCode,data);
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
		this.algData(GRAD_2018_EN,obj.getAccess_token());
		this.algData(GRAD_2018_PF,obj.getAccess_token());
		this.algData(GRAD_2004_EN,obj.getAccess_token());
		this.algData(GRAD_2004_PF,obj.getAccess_token());
		this.algData(GRAD_1996_EN,obj.getAccess_token());
		this.algData(GRAD_1996_PF,obj.getAccess_token());
		this.algData(GRAD_1986_EN,obj.getAccess_token());
		this.algData("1950",obj.getAccess_token());
		this.algData("SCCP",obj.getAccess_token());
		this.algData("NOPROG",obj.getAccess_token());
		logger.info("reloading cache completed..");
	}

	@Retry(name = "generalgetcall")
    UUID getOptionalProgramID(String gradProgram, String gradOptionalProgram, String accessToken) {
		ExceptionMessage exception = new ExceptionMessage();
		try {
	    	start();
	        OptionalProgram result = webClient.get()
	                .uri(String.format(constants.getOptionalProgram(), gradProgram,gradOptionalProgram))
	                .headers(h -> {
										h.setBearerAuth(accessToken);
										h.set(GradAlgorithmAPIConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
									})
	                .retrieve()
	                .bodyToMono(OptionalProgram.class)
	                .block();
	        end();
	        return result != null ? result.getOptionalProgramID() : null;
        } catch (Exception e) {
        	exception.setExceptionName(EXCEPTION_MESSAGE);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
    }
}
