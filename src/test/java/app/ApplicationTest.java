package app;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("default,h2")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ApplicationTest {

	// actuator
	@Test
	public void testActuatorHealth() throws Exception {
		get("/actuator/health")
		.then()
		.assertThat()
		.body("status", equalTo("UP"));
	}

	// RESTful APIs
	@Test
	public void testFindStudentById() {
		get("/api/v1/students/10001")
		.then()
		.assertThat()
		.body("id", equalTo(10001))
		.body("nid", equalTo("A1111111"))
		.body("name", equalTo("Tom"));
	}

}
