package com.wings2aspirations.genericleadcreation.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductListModel extends ItemModel{
    @SerializedName("PRODUCT_ID")
    @Expose
    private Integer pRODUCTID;
    @SerializedName("PRODUCT_NAME_VC")
    @Expose
    private String pRODUCTNAMEVC;
    @SerializedName("UNIT_ID")
    @Expose
    private Integer uNITID;
    @SerializedName("PRICE_N")
    @Expose
    private Integer pRICEN;

    public Integer getPRODUCTID() {
        return pRODUCTID;
    }

    public void setPRODUCTID(Integer pRODUCTID) {
        this.pRODUCTID = pRODUCTID;
    }

    public String getPRODUCTNAMEVC() {
        return pRODUCTNAMEVC;
    }

    public void setPRODUCTNAMEVC(String pRODUCTNAMEVC) {
        this.pRODUCTNAMEVC = pRODUCTNAMEVC;
    }

    public Integer getUNITID() {
        return uNITID;
    }

    public void setUNITID(Integer uNITID) {
        this.uNITID = uNITID;
    }

    public Integer getPRICEN() {
        return pRICEN;
    }

    public void setPRICEN(Integer pRICEN) {
        this.pRICEN = pRICEN;
    }
    @Override
    public Integer getITEMID() {
        return pRODUCTID;
    }

    @Override
    public String getITEMNAME() {
        return pRODUCTNAMEVC;
    }
}