package app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/negative")
public class NegativeController {

	@GetMapping("/slowness")
	public ResponseEntity<?> slowness(@RequestParam(value = "delay", defaultValue = "5") String delay_seconds) {
		int delay = 5;
		try {
			delay = Integer.parseInt(delay_seconds);
		} catch (Exception e){}

		try {
			Thread.sleep(delay * 1000);
		} catch (InterruptedException e) {}

		return new ResponseEntity<>(HttpStatus.OK);
	}

}