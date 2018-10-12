package com.wings2aspirations.genericleadcreation.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.wings2aspirations.genericleadcreation.models.City;
import com.wings2aspirations.genericleadcreation.models.ItemModel;
import com.wings2aspirations.genericleadcreation.models.ProductListModel;
import com.wings2aspirations.genericleadcreation.models.State;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    private static ArrayList<City> cities;
    private static ArrayList<State> states;
    private static ArrayList<ProductListModel> products;
    private static ArrayList<ItemModel> statuses;

    public static ArrayList<City> getCities() {
        return cities == null ? new ArrayList<City>() : cities;
    }

    public static void setCities(ArrayList<City> cities) {
        Constants.cities = cities;
    }

    public static ArrayList<State> getStates() {
        return states;
    }

    public static void setStates(ArrayList<State> states) {
        Constants.states = states;
    }

    public static ArrayList<ProductListModel> getProducts() {
        return products == null ? new ArrayList<ProductListModel>() : products;
    }

    public static void setProducts(ArrayList<ProductListModel> products) {
        Constants.products = products;
    }

    public static ArrayList<ItemModel> getStatuses() {
        return statuses == null ? new ArrayList<ItemModel>() : statuses;
    }

    public static void setStatuses(ArrayList<ItemModel> statuses) {
        Constants.statuses = statuses;
    }


    public static Drawable getSymbol(Context context, String symbol, float textSize, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(color);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent();
        int width = (int) (paint.measureText(symbol) + 0.5f);
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(symbol, 0, baseline, paint);
        return new BitmapDrawable(context.getResources(), image);
    }
}