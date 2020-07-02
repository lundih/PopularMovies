package com.lundih.android.popularmovies;

import android.content.Context;
import android.util.DisplayMetrics;

public class ColumnNumberCalcUtils {// This class allows us to calculate the optimal number of columns to be displayed by a recyclerview in grid layout manager
    public static int calculate(Context context, float columnWidthInDp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;

        return (int) (screenWidthDp / columnWidthInDp + 0.5); // +0.5 for correct rounding to int
    }
}
