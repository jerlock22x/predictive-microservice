package org.example.predictive_microservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PredictionControllerDynamicTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testPredictWithRealisticValues() throws Exception {

        Instances template = new ConverterUtils.DataSource(
                getClass().getClassLoader().getResource("model/template.arff").openStream()
        ).getDataSet();
        template.setClassIndex(template.numAttributes() - 1);


        Map<String, Object> inputJson = new LinkedHashMap<>();
        for (int i = 0; i < template.numAttributes(); i++) {
            String rawName = template.attribute(i).name();
            String attrName = rawName.replaceAll("[^\\x20-\\x7E]", ""); // curățare BOM
            if (i == template.classIndex()) continue;

            switch (attrName) {
                case "UDI" -> inputJson.put(attrName, 1234.0);
                case "Product ID" -> {

                    String value = template.attribute(i).value(0);
                    inputJson.put(attrName, value);
                }
                case "Type" -> {
                    String value = template.attribute(i).value(0);
                    inputJson.put(attrName, value);
                }
                case "Air temperature [K]" -> inputJson.put(attrName, 300.0);
                case "Process temperature [K]" -> inputJson.put(attrName, 310.0);
                case "Rotational speed [rpm]" -> inputJson.put(attrName, 1500.0);
                case "Torque [Nm]" -> inputJson.put(attrName, 35.5);
                case "Tool wear [min]" -> inputJson.put(attrName, 125.0);
                default -> inputJson.put(attrName, 0.0);
            }

        }


        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(inputJson);
        System.out.println("Generated JSON: " + jsonString);


        mockMvc.perform(post("/api/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prediction").exists())
                .andExpect(jsonPath("$.prediction").isNotEmpty());
    }
}
