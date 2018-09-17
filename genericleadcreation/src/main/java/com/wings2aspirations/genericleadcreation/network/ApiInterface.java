package com.wings2aspirations.genericleadcreation.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wings2aspirations.genericleadcreation.models.AuthorisationToken;
import com.wings2aspirations.genericleadcreation.models.City;
import com.wings2aspirations.genericleadcreation.models.LeadDetail;
import com.wings2aspirations.genericleadcreation.models.State;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("/getAllLeads/")
    Call<JsonArray> getAllLeads();

    @GET("/getAllLeads/")
    Call<JsonArray> getAllLeads(@Query("empId") long empId);

    @Multipart
    @POST("/insertLead/")
    Call<JsonObject> insertLead(@Query("data") JsonObject jsonObject, @Part MultipartBody.Part file);

    @POST("/insertLead/")
    Call<JsonObject> insertLead(@Query("data") JsonObject jsonObject);

    @GET("/insertProduct/{PRODUCT_NAME_VC}/{UNIT_ID}/{PRICE_N}")
    Call<JsonObject> insertProduct(@Path("PRODUCT_NAME_VC") String PRODUCT_NAME_VC, @Path("UNIT_ID") int UNIT_ID, @Path("PRICE_N") double PRICE_N);

    @GET("/insertStatusUnit/{NAME_VC}/{WHICH_TO_ADD}")
    Call<JsonObject> insertStatusUnit(@Path("NAME_VC") String NAME_VC, @Path("WHICH_TO_ADD") int WHICH_TO_ADD);

    @GET("/insertUnit/{UNIT_VC}")
    Call<JsonObject> insertUnit(@Path("UNIT_VC") String UNIT_VC);

    @Multipart
    @POST("/updateLead/")
    Call<JsonObject> updateLead(@Query("data") JsonObject jsonObject, @Part MultipartBody.Part file);

    @GET("/deleteLeadById/")
    Call<JsonObject> deleteLeadById(@Query("id") long id);

    @GET("/getLeadById/")
    Call<LeadDetail> getLeadById(@Query("id") long id);

    @GET("/downloadBusinessCard/")
    Call<ResponseBody> getBusinessCard(@Query("id") int id);

    @GET("/getAuthToken/")
    Call<AuthorisationToken> getAuthToken(@Query("ID") int id);


    @GET("/getProductList/")
    Call<JsonArray> getProductList();

    @GET("/getStatusList/")
    Call<JsonArray> getStatusList();

    @GET("/getTrialLeadById/")
    Call<JsonArray> getTrialLeadById(@Query("CHILD_FOLLOW_UP_ID") long CHILD_FOLLOW_UP_ID);

    @GET("/getUnits/")
    Call<JsonArray> getUnits();

    @GET("/getStates/")
    Call<List<State>> getStates();

    @GET("/getCities/")
    Call<List<City>> getCityList(@Query("id") int id);
}