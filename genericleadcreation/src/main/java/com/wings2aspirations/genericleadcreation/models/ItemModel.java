package com.wings2aspirations.genericleadcreation.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemModel implements Serializable{
    @SerializedName("ITEM_ID")
    @Expose
    private Integer iTEMID;
    @SerializedName("ITEM_NAME")
    @Expose
    private String iTEMNAME;

    public ItemModel() {
    }

    public ItemModel(Integer iTEMID, String iTEMNAME) {
        this.iTEMID = iTEMID;
        this.iTEMNAME = iTEMNAME;
    }

    public Integer getITEMID() {
        return iTEMID;
    }

    public void setITEMID(Integer iTEMID) {
        this.iTEMID = iTEMID;
    }

    public String getITEMNAME() {
        return iTEMNAME;
    }

    public void setITEMNAME(String iTEMNAME) {
        this.iTEMNAME = iTEMNAME;
    }

    @Override
    public String toString() {
        return iTEMNAME;
    }
}
