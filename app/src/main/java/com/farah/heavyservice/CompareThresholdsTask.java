package com.farah.heavyservice;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Created by Georgi on 11/9/2016.
 */
public class CompareThresholdsTask extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private Float val;
    private String App;
    private String Threshold;

    public CompareThresholdsTask(Context context, Float value, String AppName, String threshold) {
        mContext = context;
        val = value;
        App = AppName;
        Threshold = threshold;
    }

    private static boolean compareThProb(double value, double mean1, double mean2, double std1, double std2) {
        // double percentile = 0;
        // double percentile1 = 0;
        double percentile2 = 0;
        double rand = Math.random();

        NormalDistribution dist = new NormalDistribution();
        //percentile1 = dist.cumulativeProbability((value - mean1) / std1);
        percentile2 = dist.cumulativeProbability((value - mean2) / std2);

        //percentile = percentile1 * percentile2;

        return percentile2 > rand;

    }

    private static void compareThreshold(Float value, String Threshold, String AppName, Context context) {
        if (CommonVariables.thresholdsAvailable) {
            try {
                if (!AppName.equals("com.farah.heavyservice") && CommonVariables.thresholdsMap.get(AppName) != null && CommonVariables.thresholdsMap.get(AppName).get(Threshold) != null) {
                    double thresh_value_mean1 = Double.valueOf(CommonVariables.thresholdsMap.get(AppName).get(Threshold).get("mean"));
                    double thresh_value_std1 = Double.valueOf(CommonVariables.thresholdsMap.get(AppName).get(Threshold).get("std"));
                    double thresh_value_mean2 = Double.valueOf(CommonVariables.thresholdsMap.get("All").get(Threshold).get("mean"));
                    double thresh_value_std2 = Double.valueOf(CommonVariables.thresholdsMap.get("All").get(Threshold).get("std"));
                    if (thresh_value_mean1 != 0 && thresh_value_mean2 != 0 && thresh_value_std1 != 0 && thresh_value_std2 != 0) {
                        if (compareThProb(value, thresh_value_mean1, thresh_value_mean2, thresh_value_std1, thresh_value_std2)) {
                            new SendAlarmTask(context).execute(AppName, Threshold);
                        }
                    }
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        } else {
            if (!CommonVariables.RequestedThresholds)
                Common.getThresholds(context);
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        compareThreshold(val, Threshold, App, mContext);
        return null;
    }

}
