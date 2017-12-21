package com.example;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.stat.StatUtils;

import java.util.ArrayList;

@Getter
@Setter
public class Statistics {
    private ArrayList<Double> elapsedTimeInMs = new ArrayList<Double>();
    private double consumedRUs = 0;
    private String DELIMITER = "";

    @Override
    public String toString() {
        double[] elapsedTimes = elapsedTimeInMs.stream().mapToDouble(d -> d).toArray();
        StringBuilder builder = new StringBuilder();
        builder.append(" Consumed RU: ").append(consumedRUs).append(DELIMITER);
        builder.append(" Min time: ").append(StatUtils.min(elapsedTimes)).append(DELIMITER);
        builder.append(" Max time: ").append(StatUtils.max(elapsedTimes)).append(DELIMITER);
        builder.append(" 50th percentile: ").append(StatUtils.percentile(elapsedTimes, 50)).append(DELIMITER);
        builder.append(" 75th percentile: ").append(StatUtils.percentile(elapsedTimes, 75)).append(DELIMITER);
        builder.append(" 90th percentile: ").append(StatUtils.percentile(elapsedTimes, 90)).append(DELIMITER);
        builder.append(" 99th percentile: ").append(StatUtils.percentile(elapsedTimes, 99)).append(DELIMITER);
        builder.append(" 99.5th percentile: ").append(StatUtils.percentile(elapsedTimes, 99.5)).append(DELIMITER);
        return builder.toString();
    }
}