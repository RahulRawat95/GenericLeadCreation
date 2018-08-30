package com.wings2aspirations.genericleadcreation.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.wings2aspirations.genericleadcreation.R;
import com.wings2aspirations.genericleadcreation.activity.ListLeadsActivity;
import com.wings2aspirations.genericleadcreation.repository.CalendarHelper;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class LeadDetail implements CalendarHelper.CalendarInstance {
    @SerializedName("ID")
    @Expose
    private int ID;
    @SerializedName("COMPANY_NAME")
    @Expose
    private String COMPANY_NAME;
    @SerializedName("CONTACT_PERSON")
    @Expose
    private String CONTACT_PERSON;
    @SerializedName("EMAIL")
    @Expose
    private String EMAIL;
    @SerializedName("MOBILE_NO")
    @Expose
    private String MOBILE_NO;
    @SerializedName("ADDRESS")
    @Expose
    private String ADDRESS;
    @SerializedName("PIN_CODE")
    @Expose
    private String PIN_CODE;
    @SerializedName("CUSTOMER_REMARKS")
    @Expose
    private String CUSTOMER_REMARKS;
    @SerializedName("LEAD_REMARKS")
    @Expose
    private String LEAD_REMARKS;
    @SerializedName("NEXT_FOLLOW_UP_DATE")
    @Expose
    private String NEXT_FOLLOW_UP_DATE;
    @SerializedName("NEXT_FOLLOW_UP_TIME")
    @Expose
    private String NEXT_FOLLOW_UP_TIME;
    @SerializedName("LATITUDE")
    @Expose
    private Double LATITUDE;
    @SerializedName("LONGITUDE")
    @Expose
    private Double LONGITUDE;
    @SerializedName("CALL_TYPE")
    @Expose
    private String CALL_TYPE;
    @SerializedName("EMP_ID")
    @Expose
    private int EMP_ID;
    @SerializedName("EMP_NAME")
    @Expose
    private String EMP_NAME;
    @SerializedName("DATE_VC")
    @Expose
    private String DATE_VC;
    @SerializedName("CHILD_FOLLOW_UP_ID")
    @Expose
    private Integer CHILD_FOLLOW_UP_ID;
    @SerializedName("PRODUCT_VC")
    @Expose
    private String PRODUCT_VC;
    @SerializedName("DATE_OF_BIRTH_VC")
    @Expose
    private String DATE_OF_BIRTH_VC;
    @SerializedName("MARRIAGE_DATE_VC")
    @Expose
    private String MARRIAGE_DATE_VC;


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getCOMPANY_NAME() {
        return COMPANY_NAME;
    }

    public void setCOMPANY_NAME(String COMPANY_NAME) {
        this.COMPANY_NAME = COMPANY_NAME;
    }

    public String getCONTACT_PERSON() {
        return CONTACT_PERSON;
    }

    public void setCONTACT_PERSON(String CONTACT_PERSON) {
        this.CONTACT_PERSON = CONTACT_PERSON;
    }

    public String getEMAIL() {
        return EMAIL;
    }

    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    public String getMOBILE_NO() {
        return MOBILE_NO;
    }

    public void setMOBILE_NO(String MOBILE_NO) {
        this.MOBILE_NO = MOBILE_NO;
    }

    public String getADDRESS() {
        return ADDRESS;
    }

    public void setADDRESS(String ADDRESS) {
        this.ADDRESS = ADDRESS;
    }

    public String getPIN_CODE() {
        return PIN_CODE;
    }

    public void setPIN_CODE(String PIN_CODE) {
        this.PIN_CODE = PIN_CODE;
    }

    public String getCUSTOMER_REMARKS() {
        return CUSTOMER_REMARKS;
    }

    public void setCUSTOMER_REMARKS(String CUSTOMER_REMARKS) {
        this.CUSTOMER_REMARKS = CUSTOMER_REMARKS;
    }

    public String getLEAD_REMARKS() {
        return LEAD_REMARKS;
    }

    public void setLEAD_REMARKS(String LEAD_REMARKS) {
        this.LEAD_REMARKS = LEAD_REMARKS;
    }

    public String getNEXT_FOLLOW_UP_DATE() {
        return NEXT_FOLLOW_UP_DATE;
    }

    public void setNEXT_FOLLOW_UP_DATE(String NEXT_FOLLOW_UP_DATE) {
        this.NEXT_FOLLOW_UP_DATE = NEXT_FOLLOW_UP_DATE;
    }

    public String getNEXT_FOLLOW_UP_TIME() {
        return NEXT_FOLLOW_UP_TIME;
    }

    public void setNEXT_FOLLOW_UP_TIME(String NEXT_FOLLOW_UP_TIME) {
        this.NEXT_FOLLOW_UP_TIME = NEXT_FOLLOW_UP_TIME;
    }

    public Double getLATITUDE() {
        return LATITUDE;
    }

    public void setLATITUDE(Double LATITUDE) {
        this.LATITUDE = LATITUDE;
    }

    public Double getLONGITUDE() {
        return LONGITUDE;
    }

    public void setLONGITUDE(Double LONGITUDE) {
        this.LONGITUDE = LONGITUDE;
    }

    public String getCALL_TYPE() {
        return CALL_TYPE;
    }

    public void setCALL_TYPE(String CALL_TYPE) {
        this.CALL_TYPE = CALL_TYPE;
    }

    public int getEMP_ID() {
        return EMP_ID;
    }

    public void setEMP_ID(int EMP_ID) {
        this.EMP_ID = EMP_ID;
    }

    public String getEMP_NAME() {
        return EMP_NAME;
    }

    public void setEMP_NAME(String EMP_NAME) {
        this.EMP_NAME = EMP_NAME;
    }

    public String getDATE_VC() {
        return DATE_VC;
    }

    public void setDATE_VC(String DATE_VC) {
        this.DATE_VC = DATE_VC;
    }

    public Integer getCHILD_FOLLOW_UP_ID() {
        return CHILD_FOLLOW_UP_ID;
    }

    public void setCHILD_FOLLOW_UP_ID(Integer CHILD_FOLLOW_UP_ID) {
        this.CHILD_FOLLOW_UP_ID = CHILD_FOLLOW_UP_ID;
    }

    public String getPRODUCT_VC() {
        return PRODUCT_VC;
    }

    public void setPRODUCT_VC(String PRODUCT_VC) {
        this.PRODUCT_VC = PRODUCT_VC;
    }

    public String getDATE_OF_BIRTH_VC() {
        return DATE_OF_BIRTH_VC;
    }

    public void setDATE_OF_BIRTH_VC(String DATE_OF_BIRTH_VC) {
        this.DATE_OF_BIRTH_VC = DATE_OF_BIRTH_VC;
    }

    public String getMARRIAGE_DATE_VC() {
        return MARRIAGE_DATE_VC;
    }

    public void setMARRIAGE_DATE_VC(String MARRIAGE_DATE_VC) {
        this.MARRIAGE_DATE_VC = MARRIAGE_DATE_VC;
    }



    private Date date;

    public Date getDate() {
        if (date == null) {
            try {
                date = ListLeadsActivity.simpleDateFormat.parse(DATE_VC);
            } catch (ParseException e) {
                date = new Date();
            }
        }
        return date;
    }

    public String[] getColumnData() {
        String[] columnData = new String[]{COMPANY_NAME,
                CONTACT_PERSON,
                EMAIL,
                MOBILE_NO,
                ADDRESS,
                PIN_CODE,
                CUSTOMER_REMARKS,
                LEAD_REMARKS,
                NEXT_FOLLOW_UP_DATE,
                NEXT_FOLLOW_UP_TIME,
                CALL_TYPE,
                EMP_NAME,
                DATE_VC};
        return columnData;
    }

    @Override
    public long getFromDate(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDate());
        calendar.add(Calendar.DATE, -1);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            String values[] = sharedPreferences.getString(context.getString(R.string.key_remind_at_time), "18:00").split(":");

            calendar.set(Calendar.HOUR, Integer.parseInt(values[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(values[1]));
        } catch (Exception e) {
            calendar.set(Calendar.HOUR, 18);
            calendar.set(Calendar.MINUTE, 0);
        }
        return calendar.getTimeInMillis();
    }

    @Override
    public long getToDate(Context context) {
        return getFromDate(context) + 900000;
    }

    @Override
    public String getTitle() {
        return "Follow up with " + COMPANY_NAME;
    }

    @Override
    public String getDescription() {
        return CONTACT_PERSON + ", " + MOBILE_NO + " , " + LEAD_REMARKS;
    }

    @Override
    public String getLocation() {
        return ADDRESS;
    }

    @Override
    public String getEmails() {
        return EMAIL;
    }

    @Override
    public String getSyncId() {
        return "2013912" + String.valueOf(ID);
    }
}