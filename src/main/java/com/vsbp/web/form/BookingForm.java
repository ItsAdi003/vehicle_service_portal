package com.vsbp.web.form;

import com.vsbp.domain.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BookingForm {

    @NotNull(message = "Please choose a slot")
    private Long slotId;

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 15, message = "Vehicle number must be at most 15 characters")
    private String vehicleNumber;

    @NotBlank(message = "Vehicle model is required")
    @Size(max = 60, message = "Vehicle model must be at most 60 characters")
    private String vehicleModel;

    @NotNull(message = "Please choose a service type")
    private ServiceType serviceType;

    @Size(max = 255, message = "Notes must be at most 255 characters")
    private String notes;

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
