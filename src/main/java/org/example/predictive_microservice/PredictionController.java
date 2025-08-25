package org.example.predictive_microservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import weka.classifiers.trees.RandomForest;
import weka.core.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PredictionController {

    private RandomForest model;
    private Instances template;

    public PredictionController() throws Exception {
        // Încarcă modelul RandomForest din resources
        InputStream modelStream = getClass().getClassLoader().getResourceAsStream("model/randomforest.model");
        if (modelStream == null) {
            throw new FileNotFoundException("Modelul nu a fost găsit în resources/model/randomforest.model");
        }
        ObjectInputStream ois = new ObjectInputStream(modelStream);
        model = (RandomForest) ois.readObject();
        ois.close();

        // Încarcă structura ARFF din resources
        InputStream arffStream = getClass().getClassLoader().getResourceAsStream("model/template.arff");
        if (arffStream == null) {
            throw new FileNotFoundException("Fișierul ARFF nu a fost găsit în resources/model/template.arff");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(arffStream));
        template = new Instances(reader);
        template.setClassIndex(template.numAttributes() - 1);
        reader.close();
    }

    @PostMapping("/predict")
    public ResponseEntity<Map<String, String>> predict(@RequestBody Map<String, Object> input) throws Exception {
        Instance instance = new DenseInstance(template.numAttributes());
        instance.setDataset(template);

        for (int i = 0; i < template.numAttributes(); i++) {
            if (i == template.classIndex()) continue;

            String attrName = template.attribute(i).name();
            if (!input.containsKey(attrName)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Atribut lipsă: " + attrName));
            }

            Object value = input.get(attrName);
            Attribute attr = template.attribute(i);

            try {
                if (attr.isNumeric()) {
                    instance.setValue(i, ((Number) value).doubleValue());
                } else if (attr.isNominal()) {
                    instance.setValue(i, value.toString());
                } else {
                    return ResponseEntity.badRequest().body(Map.of("error", "Tip de atribut nesuportat: " + attrName));
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Eroare la atributul: " + attrName + ", detalii: " + e.getMessage()));
            }
        }

        double prediction = model.classifyInstance(instance);
        String predictedClass = template.classAttribute().value((int) prediction);

        Map<String, String> response = new HashMap<>();
        response.put("prediction", predictedClass);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public String ping() {
        return "Microserviciul funcționează!";
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerToArrowhead() {
        try {
            Map<String, Object> serviceRegistryRequest = new HashMap<>();
            serviceRegistryRequest.put("serviceDefinition", "predictive-service");

            Map<String, String> providerSystem = new HashMap<>();
            providerSystem.put("systemName", "predictive-microservice");
            providerSystem.put("address", "192.168.1.5");
            providerSystem.put("port", "8080");
            providerSystem.put("authenticationInfo", null);

            serviceRegistryRequest.put("providerSystem", providerSystem);
            serviceRegistryRequest.put("serviceUri", "/api/predict");

            List<String> interfaces = new ArrayList<>();
            interfaces.add("HTTP-SECURE-JSON");
            serviceRegistryRequest.put("interfaces", interfaces);
            serviceRegistryRequest.put("version", 1);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://192.168.1.5:8443/serviceregistry/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(serviceRegistryRequest)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.ok("Răspuns Service Registry: " + response.body());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la registrare: " + e.getMessage());
        }
    }
}
