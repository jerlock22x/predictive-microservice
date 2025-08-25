package org.example.predictive_microservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductionInputDto {

    @JsonProperty("UDI")
    private Double udi;

    @JsonProperty("Product ID")
    private String productId;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Air temperature [K]")
    private Double airTemperature;

    @JsonProperty("Process temperature [K]")
    private Double processTemperature;

    @JsonProperty("Rotational speed [rpm]")
    private Double rotationalSpeed;

    @JsonProperty("Torque [Nm]")
    private Double torque;

    @JsonProperty("Tool wear [min]")
    private Double toolWear;

    // Getters și Setters
    public Double getUdi() {
        return udi;
    }

    public void setUdi(Double udi) {
        this.udi = udi;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(Double airTemperature) {
        this.airTemperature = airTemperature;
    }

    public Double getProcessTemperature() {
        return processTemperature;
    }

    public void setProcessTemperature(Double processTemperature) {
        this.processTemperature = processTemperature;
    }

    public Double getRotationalSpeed() {
        return rotationalSpeed;
    }

    public void setRotationalSpeed(Double rotationalSpeed) {
        this.rotationalSpeed = rotationalSpeed;
    }

    public Double getTorque() {
        return torque;
    }

    public void setTorque(Double torque) {
        this.torque = torque;
    }

    public Double getToolWear() {
        return toolWear;
    }

    public void setToolWear(Double toolWear) {
        this.toolWear = toolWear;
    }
}
