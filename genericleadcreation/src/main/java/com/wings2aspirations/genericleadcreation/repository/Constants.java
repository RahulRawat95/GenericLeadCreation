package com.wings2aspirations.genericleadcreation.repository;

import com.wings2aspirations.genericleadcreation.models.City;
import com.wings2aspirations.genericleadcreation.models.ItemModel;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    private static ArrayList<City> cities;
    private static ArrayList<ItemModel> products;
    private static ArrayList<ItemModel> statuses;

    public static ArrayList<City> getCities() {
        return cities == null ? new ArrayList<City>() : cities;
    }

    public static void setCities(ArrayList<City> cities) {
        Constants.cities = cities;
    }

    public static ArrayList<ItemModel> getProducts() {
        return products == null ? new ArrayList<ItemModel>() : products;
    }

    public static void setProducts(ArrayList<ItemModel> products) {
        Constants.products = products;
    }

    public static ArrayList<ItemModel> getStatuses() {
        return statuses == null ? new ArrayList<ItemModel>() : statuses;
    }

    public static void setStatuses(ArrayList<ItemModel> statuses) {
        Constants.statuses = statuses;
    }
}