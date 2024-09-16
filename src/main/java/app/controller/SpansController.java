package app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.instana.sdk.annotation.Span;
import com.instana.sdk.support.SpanSupport;

@RestController
@RequestMapping("/api/v1/spans")
public class SpansController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpansController.class);
	private final String SERVICE_NAME = "spans-service";

	@GetMapping("/{id}")
	@Span(type = Span.Type.ENTRY, value = "my-span-in-oneStep")
	public ResponseEntity<?> oneStep(@PathVariable("id") Integer id) throws InterruptedException {
		LOGGER.info("GET /api/v1/spans/step/{}", id);

		SpanSupport.annotate("tags.service", SERVICE_NAME);
		SpanSupport.annotate("tags.endpoint", "GET /spans/{id}");
		SpanSupport.annotate("tags.call.name", "GET /spans/" + id);
		SpanSupport.annotate("params.id", String.valueOf(id));

		do_step1();

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping()
	@Span(type = Span.Type.ENTRY, value = "my-span-in-steps")
	public ResponseEntity<?> steps() throws InterruptedException {
		LOGGER.info("GET /api/v1/spans/steps");

		SpanSupport.annotate("tags.service", SERVICE_NAME);
		SpanSupport.annotate("tags.endpoint", "GET /spans/steps");
		SpanSupport.annotate("tags.call.name", "GET /spans/steps");

		do_step1();
		do_step2();

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Span(type = Span.Type.INTERMEDIATE, value = "my-span-in-do_step1")
	private void do_step1() throws InterruptedException {
		SpanSupport.annotate("tags.step", "1");

		// Do nothing but just wait 1s
		Thread.sleep(1 * 500);
	}

	@Span(type = Span.Type.INTERMEDIATE, value = "my-span-in-do_step2")
	private void do_step2() throws InterruptedException {
		SpanSupport.annotate("tags.step", "2");

		// Do nothing but just wait 1s
		Thread.sleep(1 * 500);
	}

	@GetMapping("/error")
	@Span(type = Span.Type.ENTRY, value = "my-span-with-error")
	public ResponseEntity<?> error(){
		LOGGER.info("GET /api/v1/spans/error");

		SpanSupport.annotate("tags.service", SERVICE_NAME);
		SpanSupport.annotate("tags.endpoint", "GET /spans/error");
		SpanSupport.annotate("tags.call.name", "GET /spans/error");

		try {

			// Let's introduce a bug here.
			int i = 1 / 0;

		} catch (Exception e) {
			SpanSupport.annotate("error", "true");
			LOGGER.error("Error out!", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/error2")
	@Span(type = Span.Type.ENTRY, value = "my-span-with-error2")
	public ResponseEntity<?> error2() throws InterruptedException{
		LOGGER.info("GET /api/v1/spans/error2");

		SpanSupport.annotate("tags.service", SERVICE_NAME);
		SpanSupport.annotate("tags.endpoint", "GET /spans/error2");
		SpanSupport.annotate("tags.call.name", "GET /spans/error2");

		do_step1();

		// mark the call is Erroneous
		SpanSupport.annotate("error", "true");

		LOGGER.error("It's error as I said it!", new Exception("I actually don't know what's wrong!"));

		return new ResponseEntity<>(HttpStatus.OK);
	}

}