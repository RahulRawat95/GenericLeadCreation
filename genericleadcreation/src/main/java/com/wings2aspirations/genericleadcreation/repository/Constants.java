package com.wings2aspirations.genericleadcreation.repository;

import com.wings2aspirations.genericleadcreation.models.City;

import java.util.List;

public class Constants {
    private static List<City> cities;

    public static List<City> getCities() {
        return cities;
    }

    public static void setCities(List<City> cities) {
        Constants.cities = cities;
    }
}
