package com.example.kuldeep.ecgapp.monitoring;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeakDetection {
    private static final int row_size = 230402;
    private List<Double> data;
    private Double heartRateValues;
    private Double avgHeartRate;
    private FileInputStream finSelected;

    public PeakDetection(FileInputStream selectedFin) {
        this.data = new ArrayList<>();
        this.heartRateValues = 0.0;
        this.avgHeartRate = 0.0;
        this.finSelected = selectedFin;
    }


    public List<Double> getData() {

        FileInputStream fin = this.finSelected;
        try {
            DataInputStream dataInputStream = new DataInputStream(fin);
            String nextLine;
            while ((nextLine = dataInputStream.readLine()) != null) {
                //String[] arr = nextLine.split("\t");
                String[] arr = nextLine.split(" ");
                data.add(Double.parseDouble(arr[1]));
            }

        } catch (IOException io) {
            System.out.println("IOException");
        }
        return data;
    }


    public int[] runPeakDetection(List<Double> data) {

        double LPFECG[] = new double[data.size()];

        for (int i = 13; i <= 45; i++) {
            int index = i - 12;
            if (index < 3)
                LPFECG[index] = 0.5 * (data.get(i) - 2 * data.get(i - 6) + data.get(i - 12));
            else
                LPFECG[index] = 0.5 * (2 * LPFECG[index - 1] - LPFECG[index - 2] + data.get(i) - 2 * data.get(i - 6) + data.get(i - 12));
        }

        double HPFECG[] = new double[data.size()];

        for (int i = 46; i < data.size(); i++) {
            int index = i - 12;
            int index2 = i - 45;
            LPFECG[index] = 0.5 * (2 * LPFECG[index - 1] - LPFECG[index - 2] + data.get(i) - 2 * data.get(i - 6) + data.get(i - 12));
            if (index2 < 2) {
                HPFECG[index2] = (1 / 32.0) * (32 * LPFECG[index - 16] + (LPFECG[index] - LPFECG[index - 32]));
            } else
                HPFECG[index2] = (1 / 32.0) * (32 * LPFECG[index - 16] - (HPFECG[index2 - 1] + LPFECG[index] - LPFECG[index - 32]));
        }

        int x_index_val = 0;
        for (int i = 1; i < HPFECG.length; i++) {
            if (HPFECG[i] == 0) {
                x_index_val = i;
                break;
            }
        }

        double x[] = new double[x_index_val];

        for (int i = 1; i < x_index_val; i++) {
            x[i] = -1 * HPFECG[i];
        }

        double BaseLine = getMean(x);

        double DynamicRangeUp = getMax(x) - BaseLine;
        double DynamicRangeDown = BaseLine - getMin(x);
        double thresholdUp = 0.002 * DynamicRangeUp;
        double thresholdR = 0.5 * DynamicRangeUp;
        double thresholdDown = 0.000002 * DynamicRangeDown;

        int up = 1;
        double PreviousPeak = x[1];
        int k = 0;
        double maximum = (double) -1000;
        double minimum = (double) 1000;
        int possiblePeak = 0;
        int Rpeak = 0;
        List<Integer> Rpeak_index = new ArrayList<>();
        int PeakType = 0;
        int i = 1;

        int peak_index[] = new int[data.size()];


        while (i < x.length) {
            if (x[i] > maximum)
                maximum = x[i];

            if (x[i] < minimum)
                minimum = x[i];

            if (up == 1) {
                if (x[i] < maximum) {
                    if (possiblePeak == 0) {
                        possiblePeak = i;
                    }


                    if (x[i] < (maximum - thresholdUp)) {
                        k = k + 1;
                        peak_index[k] = possiblePeak - 1;
                        minimum = x[i];
                        up = 0;
                        possiblePeak = 0;

                        if (PeakType == 0) {
                            if (x[peak_index[k]] > (BaseLine + thresholdR)) {

                                Rpeak = Rpeak + 1;
                                Rpeak_index.add(peak_index[k]);
                                PreviousPeak = x[peak_index[k]];
                            }
                        } else {
                            if ((Math.abs((x[peak_index[k]] - PreviousPeak) / PreviousPeak) > 1.5) && (x[peak_index[k]] > BaseLine + thresholdR)) {
                                Rpeak = Rpeak + 1;
                                Rpeak_index.add(peak_index[k]);
                                PreviousPeak = x[peak_index[k]];
                                PeakType = 2;
                            }
                        }
                    }
                }
            } else {
                if (x[i] > minimum) {
                    if (possiblePeak == 0)
                        possiblePeak = i;

                    if (x[i] > (minimum + thresholdDown)) {
                        k = k + 1;
                        peak_index[k] = possiblePeak - 1;
                        maximum = x[i];

                        up = 1;
                        possiblePeak = 0;
                    }
                }
            }

            i = i + 1;
        }

        return toIntArray(Rpeak_index);
    }


    private static double getMean(double[] input) {
        double sum = 0;
        for (double ip : input) {
            sum += ip;
        }
        return sum / input.length;
    }

    private static double getMax(double[] input) {

        double maxi = -1 * Double.MAX_VALUE;
        for (double x : input) {
            if (x > maxi) {
                maxi = x;
            }
        }
        return maxi;
    }

    private static double getMin(double[] input) {

        double mini = Double.MIN_VALUE;
        for (double x : input) {
            if (x < mini) {
                mini = x;
            }
        }
        return mini;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = list.get(i);
        return result;
    }

    public Double[] getHeartRate(int peakValues[]) {

        Double intervalHeartBeat;
        Double[] heartRateValues = new Double[peakValues.length - 1];
        for (int i = 0; i < peakValues.length - 1; i++) {
            intervalHeartBeat = 1.0 * peakValues[i + 1] - 1.0 * peakValues[i];
            Double heartRate = 90 * 125 / intervalHeartBeat;
            heartRateValues[i] = heartRate;
        }
        heartRateValues[heartRateValues.length - 1] = heartRateValues[0];
        return heartRateValues;
    }


    public Double getAvgHeartRate(Double[] heartRate) {
        Double avg = 0.0;
        for (Double i : heartRate) {
            avg = avg + i;
        }
        avg = avg / heartRate.length;
        return avg;
    }

}
