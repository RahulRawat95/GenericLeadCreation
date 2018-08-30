package com.wings2aspirations.genericleadcreation.models;

import com.google.gson.annotations.SerializedName;

public class City extends ItemModel {
    @SerializedName("ID")
    private long id;
    @SerializedName("CITY_NAME_VC")
    private String cityName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public Integer getITEMID() {
        return (int) id;
    }

    @Override
    public String getITEMNAME() {
        return cityName;
    }
}
