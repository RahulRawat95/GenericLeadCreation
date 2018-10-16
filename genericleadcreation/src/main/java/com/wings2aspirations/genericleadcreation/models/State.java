package com.wings2aspirations.genericleadcreation.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class State extends ItemModel{
    @SerializedName("ID")
    @Expose
    private int iD;
    @SerializedName("STATE_NAME_VC")
    @Expose
    private String sTATENAMEVC;

    public int getID() {
        return iD;
    }

    public void setID(int iD) {
        this.iD = iD;
    }

    public String getSTATENAMEVC() {
        return sTATENAMEVC;
    }

    public void setSTATENAMEVC(String sTATENAMEVC) {
        this.sTATENAMEVC = sTATENAMEVC;
    }

    @Override
    public Integer getITEMID() {
        return (int) iD;
    }

    @Override
    public String getITEMNAME() {
        return sTATENAMEVC;
    }
}