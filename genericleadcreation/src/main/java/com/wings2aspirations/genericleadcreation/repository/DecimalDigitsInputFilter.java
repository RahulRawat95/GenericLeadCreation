package com.wings2aspirations.genericleadcreation.repository;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalDigitsInputFilter implements InputFilter {

    Pattern mPattern;

    public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
        mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero) + "}+((\\.[0-9]{0," + (digitsAfterZero) + "})?)||(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        Matcher matcher = null;
        matcher = mPattern.matcher(dest.toString() + source);
        if (!matcher.matches())
            return "";
        return null;
    }

}
