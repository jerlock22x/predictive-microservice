package org.example.predictive_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class PredictiveMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PredictiveMicroserviceApplication.class, args);
	}


	@Bean
	CommandLineRunner registerAtStartup(org.springframework.core.env.Environment env) {
		return args -> {
			try {
				String sr = env.getProperty("arrowhead.sr");
				String systemName = env.getProperty("app.systemName");
				String serviceDef = env.getProperty("app.serviceDef");
				String serviceUri = env.getProperty("app.serviceUri");
				String iface = env.getProperty("app.interface", "HTTP-JSON");
				int port = Integer.parseInt(env.getProperty("server.port", "8080"));

				Map<String, Object> body = new java.util.LinkedHashMap<>();
				body.put("providedService", java.util.Map.of("serviceDefinition", serviceDef, "interfaces", java.util.List.of(iface)));
				body.put("providerSystem", java.util.Map.of("systemName", systemName, "address", "localhost", "port", port));
				body.put("serviceUri", serviceUri);
				body.put("secure", "NOT_SECURE");
				body.put("metadata", java.util.Map.of("version", "1"));
				body.put("version", 1);

				var rt = new org.springframework.web.client.RestTemplate();
				var headers = new org.springframework.http.HttpHeaders();
				headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

				String user = env.getProperty("arrowhead.sr.user");
				String pass = env.getProperty("arrowhead.sr.pass");
				if (user != null && pass != null) {
					headers.setBasicAuth(user, pass);
				}

				var resp = rt.postForEntity(sr + "/register",
						new org.springframework.http.HttpEntity<>(body, headers), String.class);
				System.out.println("SR register: " + resp.getStatusCode());
			} catch (Exception ex) {
				System.err.println("SR register failed: " + ex.getMessage());
			}
		};
	}
}

