package app.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * This is a temporary controller to redirect "/" to Swagger UI
 * Please note that the endpoint has been changed from v2 to v3:
 * In v2, it's at: /swagger-ui.html
 * While in v3, it's at: /swagger-ui/index.html
 *
 */
@RestController("/")
public class DefaultController {

	@GetMapping(value = "/")
    public void redirect(HttpServletResponse response) throws IOException {
        //response.sendRedirect("/swagger-ui.html");
        response.sendRedirect("/swagger-ui/index.html");
    }
	
}
