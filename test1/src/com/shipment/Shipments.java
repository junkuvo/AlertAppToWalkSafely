package com.shipment;

public class Shipments {
    private String externShipmentKey;
    private String status;
    private String expectedShipmentDate;
    private String shipmentDate;
    private String supplierName;
    public ShipmentDetails[] details;
    private String qtyExpected;
    private String qtyReceived;


    public Shipments() {
        setStatus("");
        setExternShipmentKey("");
        setShipmentDate("");
        setExpectedShipmentDate("");
        setSupplierName("");
        setQtyExpected("");
        setQtyReceived("");
    }

    public String getExternShipmentKey() {
        return externShipmentKey;
    }

    public void setExternShipmentKey(String externShipmentKey) {
        this.externShipmentKey = externShipmentKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpectedShipmentDate() {
        return expectedShipmentDate;
    }

    public void setExpectedShipmentDate(String expectedShipmentDate) {
        this.expectedShipmentDate = expectedShipmentDate;
    }

    public String getShipmentDate() {
        return shipmentDate;
    }

    public void setShipmentDate(String shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getQtyExpected() {
        return qtyExpected;
    }

    public void setQtyExpected(String qtyExpected) {
        this.qtyExpected = qtyExpected;
    }

    public String getQtyReceived() {
        return qtyReceived;
    }

    public void setQtyReceived(String qtyReceived) {
        this.qtyReceived = qtyReceived;
    }


}
