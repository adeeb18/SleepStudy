package com.example.SleepStudy;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class fragment2 extends Fragment implements SensorEventListener {

    //Light Sensor
    SensorManager sm;
    Sensor lightSensor;
    private Context context;
    //Printing to Screen
    TextView lumenValues;
    boolean listenOn;
    ConstraintLayout viewSensor;
    Button startSleepButton;
    Button pauseSleepButton;
    //Writing to file



    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2_layout, container, false);

        lumenValues = (TextView) view.findViewById(R.id.lumen_values);
        viewSensor = (ConstraintLayout) view.findViewById(R.id.constraintLayout2);

        sm = (SensorManager) getActivity().getSystemService(Service.SENSOR_SERVICE);

        lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        viewSensor.setVisibility(View.INVISIBLE);


        startSleepButton = (Button) view.findViewById(R.id.begin_sleep_button);

        startSleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenOn = true;
                viewSensor.setVisibility(View.VISIBLE);
            }
        });

        pauseSleepButton = (Button) view.findViewById(R.id.pause_sleep_button);
        pauseSleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenOn = false;
                viewSensor.setVisibility(View.INVISIBLE);
                String s = readFromFile(getActivity().getApplicationContext());
                graphLumens(s);
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
                lumenValues.setText("Light Value: " + event.values[0]);
                float val = event.values[0];
                writeToFile(String.valueOf(val), getActivity().getApplicationContext());
                Log.d("Written to file", "onSensorChanged: ");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void writeToFile(String data, Context context)  {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput("light.txt", Context.MODE_APPEND));
            osw.write(data);
            osw.write(",");
            osw.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {
        String ret = "";
        try {
            InputStream is = context.openFileInput("light.txt");
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
        String[] split = s.split(",", 2);
        Log.d("First", split[0]);
        Log.d("Second", split[1]);
        String lumens = split[1];
        ArrayList<Double> al = new ArrayList<Double>();
        while(lumens.indexOf(",") != -1) {
            String[] splitNew = lumens.split(",", 2);
            double d = Double.parseDouble(splitNew[0]);
            al.add(d);
            lumens = splitNew[1];
        }
    }
}
