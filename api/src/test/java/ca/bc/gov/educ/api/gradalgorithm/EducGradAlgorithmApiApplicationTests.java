package ca.bc.gov.educ.api.gradalgorithm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import ca.bc.gov.educ.api.gradalgorithm.dto.Student;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EducGradAlgorithmApiApplicationTests {

	@Test
	void testStudentDto() {
		Student student = new Student("126214493");
		assertEquals(student.getLegalFirstName(), "Betty", "PEN doesn't match the student");
	}

}
