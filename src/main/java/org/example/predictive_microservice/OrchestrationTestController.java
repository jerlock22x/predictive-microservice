package org.example.predictive_microservice;

import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/test")
public class OrchestrationTestController {

    private final Environment env;
    public OrchestrationTestController(Environment env) { this.env = env; }

    @GetMapping("/orchestrate")
    public Object orchestrate() {
        try {
            String orch = env.getProperty("arrowhead.orch"); // http://localhost:8441/orchestrator
            String serviceDef = env.getProperty("app.serviceDef"); // predictive-service
            RestTemplate rt = new RestTemplate();

            Map<String,Object> req = new LinkedHashMap<>();
            req.put("requesterSystem", Map.of("systemName","PlannerClient","address","localhost","port",9000));
            req.put("requestedService", Map.of("serviceDefinition", serviceDef, "interfaces", List.of("HTTP-JSON")));
            req.put("orchestrationFlags", Map.of("overrideStore", true, "matchmaking", true));

            HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> resp = rt.postForEntity(orch + "/orchestration", new HttpEntity<>(req, h), Map.class);

            List<Map<String,Object>> list = (List<Map<String,Object>>) resp.getBody().get("response");
            if (list == null || list.isEmpty()) return Map.of("error","no provider found");

            Map<String,Object> first = list.get(0);
            Map<String,Object> provider = (Map<String,Object>) first.get("provider");
            String url = "http://" + provider.get("address") + ":" + provider.get("port") + first.get("serviceUri");

            Object providerResp = rt.getForObject(url.replaceAll("//","/").replace("http:/","http://"), Object.class);
            return Map.of("orchestration", first, "providerCall", providerResp);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}
