package com.wings2aspirations.genericleadcreation.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StatusModel {
    @SerializedName("STATUS_ID")
    @Expose
    private Integer sTATUSID;
    @SerializedName("STATUS_NAME_VC")
    @Expose
    private String sTATUSNAMEVC;
    @SerializedName("DEPARTMENT_VC")
    @Expose
    private String dEPARTMENTVC;

    public Integer getSTATUSID() {
        return sTATUSID;
    }

    public void setSTATUSID(Integer sTATUSID) {
        this.sTATUSID = sTATUSID;
    }

    public String getSTATUSNAMEVC() {
        return sTATUSNAMEVC;
    }

    public void setSTATUSNAMEVC(String sTATUSNAMEVC) {
        this.sTATUSNAMEVC = sTATUSNAMEVC;
    }

    public String getDEPARTMENTVC() {
        return dEPARTMENTVC;
    }

    public void setDEPARTMENTVC(String dEPARTMENTVC) {
        this.dEPARTMENTVC = dEPARTMENTVC;
    }

}