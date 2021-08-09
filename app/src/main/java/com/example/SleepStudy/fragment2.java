package com.example.SleepStudy;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.LocaleDisplayNames;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class fragment2 extends Fragment implements SensorEventListener {

    //Light Sensor
    SensorManager sm;
    Sensor lightSensor;

    private Context context;

    //Printing to Screen
    TextView lumenValues;
    boolean listenOn;
    ConstraintLayout viewSensor;

    //Buttons
    Button startSleepButton;
    Button pauseSleepButton;
    Button resetSleepButton;

    //Graphing
    //Graph against time? TODO
    GraphView lightGraph;
    Date currentTime;
    String formattedDate;

    //File resets at midnight!!! TODO
    String lightfile = "light_";


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //Initialize Sliding tabs
        View view = inflater.inflate(R.layout.fragment_2_layout, container, false);

        //Initialize Sensor and Screen View
        lumenValues = (TextView) view.findViewById(R.id.lumen_values);

        viewSensor = (ConstraintLayout) view.findViewById(R.id.constraintLayout2);
        sm = (SensorManager) getActivity().getSystemService(Service.SENSOR_SERVICE);

        lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        viewSensor.setVisibility(View.INVISIBLE);

        //Create filename
        currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        formattedDate = df.format(currentTime);
        lightfile = lightfile + formattedDate + ".txt";

        //Initialize Start Button
        startSleepButton = (Button) view.findViewById(R.id.begin_sleep_button);
        startSleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Begin sensing and view output
                listenOn = true;
                viewSensor.setVisibility(View.VISIBLE);
            }
        });

        //Initialize Pause Button
        pauseSleepButton = (Button) view.findViewById(R.id.pause_sleep_button);
        pauseSleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Turn off sensing
                listenOn = false;
                viewSensor.setVisibility(View.INVISIBLE);
                //Print to graph
                String s = readFromFile(getActivity().getApplicationContext());
                graphLumens(s);
            }
        });

        //Initialize Reset Butotn
        resetSleepButton = (Button) view.findViewById(R.id.reset_sleep_button);
        resetSleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete file
                getActivity().getApplicationContext().deleteFile(lightfile);
                resetSleepButton.setEnabled(false);
                //Remove graph series
                lightGraph.removeAllSeries();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        sm.unregisterListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        sm.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(listenOn) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                //Print to screen
                lumenValues.setText("Light Value: " + event.values[0]);
                float val = event.values[0];
                //Print to file
                writeToFile(String.valueOf(val), getActivity().getApplicationContext());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void writeToFile(String data, Context context)  {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(lightfile, Context.MODE_APPEND));
            osw.write(data);
            osw.write(",");
            osw.close();
            resetSleepButton.setEnabled(true);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {
        String ret = "";
        try {
            InputStream is = context.openFileInput(lightfile);
            if(is!=null) {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String receiveString = "";
                StringBuilder sb = new StringBuilder();
                while ( (receiveString = br.readLine()) != null ) {
                    sb.append("\n").append(receiveString);
                }
                is.close();
                ret = sb.toString();
            }
        }
        catch (FileNotFoundException e){
            Log.e("login activity", "File not found: " + e.toString());
        }
        catch (IOException e) {
            Log.e("login activity", "Cannot read file: " + e.toString());
        }
        return ret;
    }

    private void graphLumens(String s) {
        //Convert string to int array
        String[] split = s.split(",", 2);
        String lumens = split[1];
        ArrayList<Integer> al = new ArrayList<Integer>();
        while(lumens.indexOf(",") != -1) {
            String[] splitNew = lumens.split(",", 2);
            double d = Double.parseDouble(splitNew[0]);
            al.add((int)d);
            lumens = splitNew[1];
        }

        //Create series
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

        for(int i = 0; i < al.size(); i++) {
            series.appendData(new DataPoint(i,al.get(i)),true, 1000);
        }

        //Add series to graph
        lightGraph = (GraphView) getActivity().findViewById(R.id.graph);
        lightGraph.addSeries(series);
    }
}
