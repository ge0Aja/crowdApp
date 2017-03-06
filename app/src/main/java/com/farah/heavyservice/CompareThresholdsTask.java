package com.farah.heavyservice;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Created by Georgi on 11/9/2016.
 * this Async task is called to compare the collected stat for any feature against a specified threshold
 * the task takes the context and the collected value and app and the specified threshold as inputs
 *
 * it calculates the zscore of the collected value knowing the mean and standard deviation of the feature value
 *
 * then it calculates the probability score of that zscore and flips a coin (generate a random number) and if the coin
 * is smaller than the probability score of the value the task will trigger a submit alarm (event) task which submits
 * an event to the server
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
                            double prt = value / thresh_value_mean2 - 1;
                            new SendAlarmTask(context).execute(AppName, Threshold, String.valueOf(prt), String.valueOf(thresh_value_mean2));
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
