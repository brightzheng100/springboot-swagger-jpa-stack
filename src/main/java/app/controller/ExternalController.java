package app.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/external")
public class ExternalController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalController.class);

	@GetMapping("/website")
	public ResponseEntity<?> google(@RequestParam(value = "url", defaultValue = "https://www.google.com") String url) {
		LOGGER.info("GET v1/external/website?url={}", url);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			LOGGER.info("GET Response Code : {}", responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				String patternString = "<title>(.*?)</title>";
				Pattern pattern = Pattern.compile(patternString);
				Matcher matcher = pattern.matcher(response);

				String title = "NO TITLE";
				while (matcher.find()) {
					title = matcher.group(1);
				}

				LOGGER.info("title: {}", title);
				return new ResponseEntity<>(title, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.valueOf(responseCode));
			}
		} catch (Exception e) {
			LOGGER.error("error", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}