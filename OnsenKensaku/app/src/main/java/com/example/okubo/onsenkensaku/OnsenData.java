package com.example.okubo.onsenkensaku;

/**
 * Created by Okubo on 9/29/2015 029.
 */
public class OnsenData {
    private int id;
    private String name;
    private String kana;
    private String address;
    private String tel;
    private String price;
    private String close_day;
    private String open_hour;
    private String spring_quality;
    private double latitude;
    private double longitude;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getKana() {
        return kana;
    }
    public void setKana(String kana) {
        this.kana = kana;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getTel() {
        return tel;
    }
    public void setTel(String tel) {
        this.tel = tel;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }
    public String getClose_day() {
        return close_day;
    }
    public void setClose_day(String close_day) {
        this.close_day = close_day;
    }
    public String getOpen_hour() {
        return open_hour;
    }
    public void setOpen_hour(String open_hour) {
        this.open_hour = open_hour;
    }
    public String getSpring_quality() {
        return spring_quality;
    }
    public void setSpring_quality(String spring_quality) {
        this.spring_quality = spring_quality;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
