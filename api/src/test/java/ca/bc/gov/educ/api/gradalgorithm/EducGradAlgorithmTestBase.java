package ca.bc.gov.educ.api.gradalgorithm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.bc.gov.educ.api.gradalgorithm.dto.AssessmentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.CourseAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradAlgorithmGraduationStudentRecord;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradProgramAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradalgorithm.dto.GradStudentAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.dto.RuleProcessorData;
import ca.bc.gov.educ.api.gradalgorithm.dto.School;
import ca.bc.gov.educ.api.gradalgorithm.dto.StudentGraduationAlgorithmData;
import ca.bc.gov.educ.api.gradalgorithm.util.JsonTransformer;

@SpringBootTest
public class EducGradAlgorithmTestBase {

	@Autowired
	JsonTransformer jsonTransformer;

	protected RuleProcessorData createRuleProcessorData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (RuleProcessorData)jsonTransformer.unmarshall(json, RuleProcessorData.class);
	}
	
	protected GradSearchStudent createGradStudentData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (GradSearchStudent)jsonTransformer.unmarshall(json, GradSearchStudent.class);
	}
	
	protected CourseAlgorithmData createCourseAlgorithmData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (CourseAlgorithmData)jsonTransformer.unmarshall(json, CourseAlgorithmData.class);
	}
	
	protected AssessmentAlgorithmData createAssessmentAlgorithmData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (AssessmentAlgorithmData)jsonTransformer.unmarshall(json, AssessmentAlgorithmData.class);
	}
	
	protected GradStudentAlgorithmData createGradStudentAlgorithmData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (GradStudentAlgorithmData)jsonTransformer.unmarshall(json, GradStudentAlgorithmData.class);
	}
	
	
	protected GradProgramAlgorithmData createProgramAlgorithmData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (GradProgramAlgorithmData)jsonTransformer.unmarshall(json, GradProgramAlgorithmData.class);
	}

	protected String createStudentOptionalProgramData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		return readInputStream(inputStream);
	}
	
	protected StudentGraduationAlgorithmData createStudentGraduationAlgorithmData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (StudentGraduationAlgorithmData)jsonTransformer.unmarshall(json, StudentGraduationAlgorithmData.class);
	}
	
	protected GradAlgorithmGraduationStudentRecord createGradStatusData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (GradAlgorithmGraduationStudentRecord)jsonTransformer.unmarshall(json, GradAlgorithmGraduationStudentRecord.class);
	}
	
	protected School createSchoolData(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		String json = readInputStream(inputStream);
		return (School)jsonTransformer.unmarshall(json, School.class);
	}
	
	
	
	
	private String readInputStream(InputStream is) throws Exception {
		StringBuffer sb = new StringBuffer();
		InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

}
