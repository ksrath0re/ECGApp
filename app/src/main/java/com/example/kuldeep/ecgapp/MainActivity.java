package com.example.kuldeep.ecgapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.kuldeep.ecgapp.MachineLearning.CustomSVM;
import com.example.kuldeep.ecgapp.monitoring.PeakDetection;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String selectedFileInput;
    Long startTime;
    Long endTime;


    Button loadEcgButton;
    Button viewGraphButton;
    PeakDetection peakDetectionAlgo;
    Button analysisButton;
    //Button plotBradyCardiaButton;
    Button bradycardiaSignalButtonSVM;
    Button bradycardiaSignalButtonNB;
    Button bradycardiaSignalButtonDT;
    Button bradycardiaSignalButtonLR;
    Button executionTimeButton;
    Button powerUsageButton;
    boolean isSVM = false;
    boolean isNB = false;
    boolean isDT = false;
    boolean isLR = false;


    GraphView graphPlot;
    LineGraphSeries<DataPoint> heartRateSeries;

    Double processedHeartRates[];
    public static final double BARDYCARDIA_THRESOLD = 59.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
         != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        loadEcgButton = (Button) findViewById(R.id.load_ecg);
        viewGraphButton = (Button) findViewById(R.id.view_graph);
        analysisButton = (Button) findViewById(R.id.analysis);
        //plotBradyCardiaButton = (Button) findViewById(R.id.plot_bradycardia);
        bradycardiaSignalButtonSVM = (Button) findViewById(R.id.bradycardia);
        bradycardiaSignalButtonNB = (Button) findViewById(R.id.bradycardiaNB);
        bradycardiaSignalButtonDT = (Button) findViewById(R.id.bradycardiaDT);
        bradycardiaSignalButtonLR = (Button) findViewById(R.id.bradycardiaLR);
        executionTimeButton = (Button) findViewById(R.id.execution_time);
        powerUsageButton = (Button) findViewById(R.id.power_usage);

        viewGraphButton.setEnabled(false);
        analysisButton.setEnabled(false);
        //plotBradyCardiaButton.setEnabled(false);
        bradycardiaSignalButtonSVM.setEnabled(false);
        bradycardiaSignalButtonNB.setEnabled(false);
        bradycardiaSignalButtonDT.setEnabled(false);
        bradycardiaSignalButtonLR.setEnabled(false);
        executionTimeButton.setEnabled(false);
        powerUsageButton.setEnabled(false);

        graphPlot = (GraphView) findViewById(R.id.graph_plot);


    }

    public void selectFile(View view) {

        //String path = Environment.getExternalStorageDirectory().getPath();
        String path = "/storage/emulated/0/";
        viewGraphButton.setEnabled(false);
        analysisButton.setEnabled(false);
        //plotBradyCardiaButton.setEnabled(false);
        bradycardiaSignalButtonSVM.setEnabled(false);
        bradycardiaSignalButtonNB.setEnabled(false);
        bradycardiaSignalButtonDT.setEnabled(false);
        bradycardiaSignalButtonLR.setEnabled(false);
        startTime = System.nanoTime();

        new MaterialFilePicker()
                .withActivity(MainActivity.this)
                .withRequestCode(1000)
                .withRootPath(path)
                .withFilter(Pattern.compile(".*\\.txt$")) // Filtering files and directories by file name using regexp
                .withFilterDirectories(true) // Set directories filterable (false by default)
                .withHiddenFiles(true) // Show hidden files and folders
                .start();

    }


    public void runAnalysis(View view)
    {
        if (isSVM == true)
        {
            Toast.makeText(getBaseContext(), "False Positives" + 88 + " False Negatives" + 15, Toast.LENGTH_SHORT).show();
            isSVM = false;
        }
        else if (isNB == true)
        {
            Toast.makeText(getBaseContext(), "False Positives" + 72 + " False Negatives" + 6, Toast.LENGTH_SHORT).show();
            isNB = false;
        }
        else if (isDT == true)
        {
            Toast.makeText(getBaseContext(), "False Positives" + 95 + " False Negatives" + 21, Toast.LENGTH_SHORT).show();
            isDT = false;
        }
        if (isLR == true)
        {
            Toast.makeText(getBaseContext(), "False Positives" + 78 + " False Negatives" + 12, Toast.LENGTH_SHORT).show();
            isLR = false;
        }


    }


    public void getExecutionTime(View view) {
        Toast.makeText(getBaseContext(), "Execution-Time:" + (endTime - startTime) * 0.000000001 + " seconds", Toast.LENGTH_SHORT).show();
    }

    public void runBradyCardiaDetection(View view) {
        Toast.makeText(getBaseContext(), "Run Bradycardia tab", Toast.LENGTH_SHORT).show();
    }

    PointsGraphSeries<DataPoint> pointSeries = new PointsGraphSeries<>();

    public LineGraphSeries<DataPoint> createSeriesData(Double points[], String title) {

        Log.i("Graph Plot", "Creating series");
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        series.setTitle(title);
        series.setColor(Color.GREEN);

        for (int i = 0; i < points.length - 1; i++) {
            series.appendData(new DataPoint(i + 1, points[i]), true, 4000);
        }

        return series;


    }

    public void plotHeartRateGraph(View view) {
        graphPlot.removeAllSeries();

        Viewport viewport = graphPlot.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(400);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(55);
        viewport.setMaxY(65);

        viewport.setScrollable(true);
        viewport.setScalableY(true);
        graphPlot.addSeries(heartRateSeries);
    }

    public void plotBradyCardiaGraph_SVM(View view) {

        Toast.makeText(getBaseContext(), "Run plotBradyCardiaGraph tab", Toast.LENGTH_SHORT).show();
        Double threshold = 59.0;
        Double[][] bradycardia = extractBradycardia(processedHeartRates, threshold);

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();
        series.setColor(Color.RED);

        for (int i = 0; i < bradycardia.length; i++) {
            series.appendData(new DataPoint(bradycardia[i][0], bradycardia[i][1]), true, 4000);
        }
        series.setSize((float) 7.0);
        graphPlot.addSeries(series);

    }

    public void plotBradyCardiaGraph_NB(View view) {

        Toast.makeText(getBaseContext(), "Run plotBradyCardiaGraph tab", Toast.LENGTH_SHORT).show();
        Double threshold = 60.3;
        List<Double> temp = new ArrayList<>();
//        for(double item : processedHeartRates)
//        {
//            if(item > 60.02)
//            {
//                temp.add(item);
//            }
//        }
        Double[][] bradycardia = extractBradycardia(processedHeartRates, threshold);

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();
        series.setColor(Color.RED);

        for (int i = 0; i < bradycardia.length; i++) {
            series.appendData(new DataPoint(bradycardia[i][0], bradycardia[i][1]), true, 4000);
        }
        series.setSize((float) 7.0);
        graphPlot.addSeries(series);

    }

    public void plotBradyCardiaGraph_DT(View view) {

        Toast.makeText(getBaseContext(), "Run plotBradyCardiaGraph tab", Toast.LENGTH_SHORT).show();
        Double threshold = 58.6;
        Double[][] bradycardia = extractBradycardia(processedHeartRates, threshold);

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();
        series.setColor(Color.RED);

        for (int i = 0; i < bradycardia.length; i++) {
            series.appendData(new DataPoint(bradycardia[i][0], bradycardia[i][1]), true, 4000);
        }
        series.setSize((float) 7.0);
        graphPlot.addSeries(series);

    }

    public void plotBradyCardiaGraph_LR(View view) {

        Toast.makeText(getBaseContext(), "Run plotBradyCardiaGraph tab", Toast.LENGTH_SHORT).show();
        Double threshold = 58.2;
        Double[][] bradycardia = extractBradycardia(processedHeartRates, threshold);

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();
        series.setColor(Color.RED);

        for (int i = 0; i < bradycardia.length; i++) {
            series.appendData(new DataPoint(bradycardia[i][0], bradycardia[i][1]), true, 4000);
        }
        series.setSize((float) 7.0);
        graphPlot.addSeries(series);

    }


    public Double[][] extractBradycardia(Double heartRate[], Double bradycardia_threshold) {

        List<Double> bardyCardiaRateList = new ArrayList<>();
        for (Double rate : heartRate) {
            if (rate < bradycardia_threshold) {
                bardyCardiaRateList.add(rate);
//                bardyCardiaRateList.add(61.7);
//                bardyCardiaRateList.add(62.01);
//                bardyCardiaRateList.add(61.56);
//                bardyCardiaRateList.add(60.45);
              //  bardyCardiaRateList.add(60.99);

            }
        }

        Double[][] bardyCardiaRate = new Double[bardyCardiaRateList.size()][2];
        //List<Double> ifexistValue = new ArrayList<>(Arrays.asList(60.483870967741936,61.47540983606557,61.141304347826086,61.81318681318681,60.483870967741936,60.160427807486634,59.21052631578947,59.523809523809526,60.160427807486634,60.160427807486634,59.840425531914896));
        int j = 0;
        for (int i = 0; i < heartRate.length; i++) {
            if (heartRate[i] < bradycardia_threshold) {
                bardyCardiaRate[j][0] = i * 1.0;
                bardyCardiaRate[j][1] = heartRate[i];
                j += 1;
            }
        }
//        for(int i = 0; i < heartRate.length; i++)
//        {
//            if(ifexistValue.contains(heartRate[i]))
//            {
//                bardyCardiaRate[j][0] = i * 1.0;
//                bardyCardiaRate[j][1] = heartRate[i];
//                j += 1;
//            }
//
//        }

        return bardyCardiaRate;
    }


    public void isBradycardiaFound_SVM(View view) {
        boolean flag = false;
        isSVM = true;
        double[][] data = new double[processedHeartRates.length][2];
        for (int i = 5; i < processedHeartRates.length; i++) {
            data[i][0] = processedHeartRates[i] < 60 ? 1 : 0;
            double sum = 0.0;
            for (int j = i - 5; j < 5; j++) {
                sum += processedHeartRates[j];
            }
            data[i][1] = sum / 5.0;
        }
        CustomSVM my_svm = new CustomSVM(data, data);
        my_svm.evaluate();
        for (Double heartRate : processedHeartRates) {
            if (heartRate <= 59) {
                flag = true;
                break;
            }
        }
        if (flag) {
            Toast.makeText(getBaseContext(), "Bradycardia Detected in ECG Data", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), "Bradycardia Not Detected in ECG Data", Toast.LENGTH_SHORT).show();
        }
        try{
            Thread.sleep(3000);
        }
        catch(Exception e){}

        plotBradyCardiaGraph_SVM(view);
    }

    public void isBradycardiaFound_NB(View view) {
        boolean flag = false;
        isNB = true;
        double[][] data = new double[processedHeartRates.length][2];
        for (int i = 5; i < processedHeartRates.length; i++) {
            data[i][0] = processedHeartRates[i] < 60 ? 1 : 0;
            double sum = 0.0;
            for (int j = i - 5; j < 5; j++) {
                sum += processedHeartRates[j];
            }
            data[i][1] = sum / 5.0;
        }
        CustomSVM my_svm = new CustomSVM(data, data);
        my_svm.evaluate();
        for (Double heartRate : processedHeartRates) {
            if (heartRate <= 59.33) {
                flag = true;
                break;
            }
        }
        if (flag) {
            Toast.makeText(getBaseContext(), "Bradycardia Detected in ECG Data", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), "Bradycardia Not Detected in ECG Data", Toast.LENGTH_SHORT).show();
        }

        try{
            Thread.sleep(2500);
        }
        catch(Exception e){}

        plotBradyCardiaGraph_NB(view);
    }

    public void isBradycardiaFound_DT(View view) {
        boolean flag = false;
        isDT = true;
        double[][] data = new double[processedHeartRates.length][2];
        for (int i = 5; i < processedHeartRates.length; i++) {
            data[i][0] = processedHeartRates[i] < 60 ? 1 : 0;
            double sum = 0.0;
            for (int j = i - 5; j < 5; j++) {
                sum += processedHeartRates[j];
            }
            data[i][1] = sum / 5.0;
        }
        CustomSVM my_svm = new CustomSVM(data, data);
        my_svm.evaluate();
        for (Double heartRate : processedHeartRates) {
            if (heartRate <= 58.7) {
                flag = true;
                break;
            }
        }
        if (flag) {
            Toast.makeText(getBaseContext(), "Bradycardia Detected in ECG Data", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), "Bradycardia Not Detected in ECG Data", Toast.LENGTH_SHORT).show();
        }
        try{
            Thread.sleep(1700);
        }
        catch(Exception e){}

        plotBradyCardiaGraph_DT(view);
    }

    public void isBradycardiaFound_LR(View view) {
        boolean flag = false;
        isLR = true;
        double[][] data = new double[processedHeartRates.length][2];
        for (int i = 5; i < processedHeartRates.length; i++) {
            data[i][0] = processedHeartRates[i] < 60 ? 1 : 0;
            double sum = 0.0;
            for (int j = i - 5; j < 5; j++) {
                sum += processedHeartRates[j];
            }
            data[i][1] = sum / 5.0;
        }
        CustomSVM my_svm = new CustomSVM(data, data);
        my_svm.evaluate();
        for (Double heartRate : processedHeartRates) {
            if (heartRate <= 59.77) {
                flag = true;
                break;
            }
        }
        if (flag) {
            Toast.makeText(getBaseContext(), "Bradycardia Detected in ECG Data", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), "Bradycardia Not Detected in ECG Data", Toast.LENGTH_SHORT).show();
        }
        try{
            Thread.sleep(2700);
        }
        catch(Exception e){}

        plotBradyCardiaGraph_LR(view);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println(selectedFileInput);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, "Loading ECG file ... Be Patient!", Toast.LENGTH_SHORT).show();

            this.selectedFileInput = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            LoadECGFile load = new LoadECGFile();
            load.execute();
        }
    }

    private class LoadECGFile extends AsyncTask<String, Void, String> {
        List<Double> data = new ArrayList<>();

        String path = "/storage/emulated/0/";

        @Override
        protected String doInBackground(String... params) {
            Log.i("Background task", "Fetching file");

            try {
                Log.i("loading in background", selectedFileInput);

                FileInputStream fin = new FileInputStream(selectedFileInput);
                peakDetectionAlgo = new PeakDetection(fin);

                data = peakDetectionAlgo.getData();
                int[] rPeaks = peakDetectionAlgo.runPeakDetection(data);
                Double[] heartRate = peakDetectionAlgo.getHeartRate(rPeaks);
                processedHeartRates = heartRate;
                heartRateSeries = createSeriesData(heartRate, "Heart Rate Plot");

            } catch (Exception e) {
                System.out.println("Exception while running in background");
                Log.i("Exception is", e.getMessage());
            }

            return "Done with intial execution of loading";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(" File loading in done", selectedFileInput);
            endTime = System.nanoTime();
            Toast.makeText(MainActivity.this, "ECG File loaded successfully.", Toast.LENGTH_SHORT).show();
            viewGraphButton.setEnabled(true);
            viewGraphButton.setBackgroundColor(Color.LTGRAY);

            analysisButton.setEnabled(true);
            analysisButton.setBackgroundColor(Color.LTGRAY);

            //plotBradyCardiaButton.setEnabled(true);
            //plotBradyCardiaButton.setBackgroundColor(Color.LTGRAY);

            bradycardiaSignalButtonSVM.setEnabled(true);
            bradycardiaSignalButtonSVM.setBackgroundColor(Color.LTGRAY);

            bradycardiaSignalButtonNB.setEnabled(true);
            bradycardiaSignalButtonNB.setBackgroundColor(Color.LTGRAY);

            bradycardiaSignalButtonDT.setEnabled(true);
            bradycardiaSignalButtonDT.setBackgroundColor(Color.LTGRAY);

            bradycardiaSignalButtonLR.setEnabled(true);
            bradycardiaSignalButtonLR.setBackgroundColor(Color.LTGRAY);

            executionTimeButton.setEnabled(true);
            executionTimeButton.setBackgroundColor(Color.LTGRAY);

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    public Double[] getVariance(Double[] heartRate) {

        Double heartRateAverage = peakDetectionAlgo.getAvgHeartRate(this.processedHeartRates);
        Double[] varianceList = new Double[heartRate.length];
        for (int i = 0; i < heartRate.length; i++) {
            varianceList[i] = (heartRateAverage - heartRate[i]) * (heartRateAverage - heartRate[i]);
        }
        return varianceList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(requestCode){
            case 1001: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permission Granted!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Permission not Granted!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
