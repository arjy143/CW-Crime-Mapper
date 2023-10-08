package com.example.mapstest;

public class CrimeData {
    //class for getting data from the police api
    String category;
    float latitude;
    float longitude;
    String streetName;
    String month;

    public CrimeData(String category, float latitude, float longitude, String streetName, String month) {
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.streetName = streetName;
        this.month = month;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }


}
