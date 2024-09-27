package app.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/httpbin")
public class ExternalController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalController.class);
	private static final String HTTPBIN_URL = "https://httpbin.org";

	@GetMapping("/get")
	public ResponseEntity<String> get(@RequestParam(value = "header", defaultValue = "hello world") String header) {

		String resultContent = null;

		HttpGet httpGet = new HttpGet(HTTPBIN_URL + "/get");
		httpGet.setHeader("x-custom-header", header);

		// call some internal dummy method for specific test cases
		this.echo(header);
		this.reverse(header);

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			CloseableHttpResponse response = httpclient.execute(httpGet);

			resultContent = EntityUtils.toString(response.getEntity());
			LOGGER.info(resultContent);
		} catch (IOException | ParseException e) {
			LOGGER.error("Error occurred", e);
		}

		return new ResponseEntity<String>(resultContent, HttpStatus.OK);
	}

	@PostMapping("/post")
	public ResponseEntity<String> post(@RequestParam(value = "header", defaultValue = "hello world") String header) {
		String resultContent = null;

		// call some internal dummy method for specific test cases
		this.echo(header);
		this.reverse(header);

		HttpPost httpPost = new HttpPost(HTTPBIN_URL + "/post");

		// Add custom header
		httpPost.setHeader("x-custom-header", header);

		// Add Query Parameters
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("username", "myuser"));
		nvps.add(new BasicNameValuePair("password", "mysecret"));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps));

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			CloseableHttpResponse response = httpclient.execute(httpPost);

			resultContent = EntityUtils.toString(response.getEntity());
			LOGGER.info(resultContent);
		} catch (IOException | ParseException e) {
			LOGGER.error("Error occurred", e);
		}

		return new ResponseEntity<String>(resultContent, HttpStatus.OK);
	}

	// just a dummy echo method
	public String echo(String param) {
		return "echoing " + param;
	}

	// just a dummy reverse method
	public String reverse(String param) {
		return new StringBuilder(param).reverse().toString();
	}
}