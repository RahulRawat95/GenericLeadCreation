package com.wings2aspirations.genericleadcreation.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExistingCustomer {
    @SerializedName("COMPANY_NAME")
    @Expose
    private String cOMPANYNAME;
    @SerializedName("CONTACT_PERSON")
    @Expose
    private String cONTACTPERSON;
    @SerializedName("EMAIL")
    @Expose
    private String eMAIL;
    @SerializedName("MOBILE_NO")
    @Expose
    private String mOBILENO;
    @SerializedName("ADDRESS")
    @Expose
    private String aDDRESS;
    @SerializedName("PIN_CODE")
    @Expose
    private String pINCODE;
    @SerializedName("CITY_ID")
    @Expose
    private Integer cITYID;
    @SerializedName("STATE_ID")
    @Expose
    private Integer sTATEID;
    @SerializedName("DESIGNATION_VC")
    @Expose
    private String dESIGNATIONVC;
    @SerializedName("CITY_NAME_VC")
    @Expose
    private String cityNameVc;
    @SerializedName("STATE_NAME_VC")
    @Expose
    private String stateNameVc;

    public ExistingCustomer() {
    }

    public ExistingCustomer(JsonObject jsonObject, String stateNameVc, String cityNameVc) {
        cOMPANYNAME = jsonObject.get("COMPANY_NAME").getAsString();
        dESIGNATIONVC = jsonObject.get("DESIGNATION_VC").getAsString();
        cONTACTPERSON = jsonObject.get("CONTACT_PERSON").getAsString();
        eMAIL = jsonObject.get("EMAIL").getAsString();
        mOBILENO = jsonObject.get("MOBILE_NO").getAsString();
        aDDRESS = jsonObject.get("ADDRESS").getAsString();
        pINCODE = jsonObject.get("PIN_CODE").getAsString();
        cITYID = jsonObject.get("CITY_ID").getAsInt();
        sTATEID = jsonObject.get("STATE_ID").getAsInt();
        this.stateNameVc = stateNameVc;
        this.cityNameVc = cityNameVc;
    }

    public String getCOMPANYNAME() {
        return cOMPANYNAME;
    }

    public void setCOMPANYNAME(String cOMPANYNAME) {
        this.cOMPANYNAME = cOMPANYNAME;
    }

    public String getCONTACTPERSON() {
        return cONTACTPERSON;
    }

    public void setCONTACTPERSON(String cONTACTPERSON) {
        this.cONTACTPERSON = cONTACTPERSON;
    }

    public String getEMAIL() {
        return eMAIL;
    }

    public void setEMAIL(String eMAIL) {
        this.eMAIL = eMAIL;
    }

    public String getMOBILENO() {
        return mOBILENO;
    }

    public void setMOBILENO(String mOBILENO) {
        this.mOBILENO = mOBILENO;
    }

    public String getADDRESS() {
        return aDDRESS;
    }

    public void setADDRESS(String aDDRESS) {
        this.aDDRESS = aDDRESS;
    }

    public String getPINCODE() {
        return pINCODE;
    }

    public void setPINCODE(String pINCODE) {
        this.pINCODE = pINCODE;
    }

    public Integer getCITYID() {
        return cITYID;
    }

    public void setCITYID(Integer cITYID) {
        this.cITYID = cITYID;
    }

    public Integer getSTATEID() {
        return sTATEID;
    }

    public void setSTATEID(Integer sTATEID) {
        this.sTATEID = sTATEID;
    }

    public String getDESIGNATIONVC() {
        return dESIGNATIONVC;
    }

    public void setDESIGNATIONVC(String dESIGNATIONVC) {
        this.dESIGNATIONVC = dESIGNATIONVC;
    }

    public String getCityNameVc() {
        return cityNameVc;
    }

    public void setCityNameVc(String cityNameVc) {
        this.cityNameVc = cityNameVc;
    }

    public String getStateNameVc() {
        return stateNameVc;
    }

    public void setStateNameVc(String stateNameVc) {
        this.stateNameVc = stateNameVc;
    }

    @Override
    public String toString() {
        return cOMPANYNAME + " (" + cityNameVc + ")";
    }
}