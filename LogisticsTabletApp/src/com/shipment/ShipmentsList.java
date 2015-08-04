package com.shipment;

public class ShipmentsList {
    private String status;
    private String message;
    public Shipments[] shipments;

    public ShipmentsList() {
        setStatus("");
        setMessage("");
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
