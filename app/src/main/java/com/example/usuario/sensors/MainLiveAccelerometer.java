package com.example.usuario.sensors;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import static java.lang.Math.pow;


public class MainLiveAccelerometer extends ActionBarActivity implements SensorEventListener {

    //Code for sensors taken from
    //https://developer.android.com/guide/topics/sensors/sensors_overview.html

    //Code for charts taken from
    //http://www.android-graphview.org/documentation/how-to-create-a-simple-graph
    //http://www.android-graphview.org/documentation/realtime-updates


    private static final String TAG = MainLiveAccelerometer.class.getSimpleName();

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private static final int X_AXIS_INDEX = 0;

    private int contX = 0;
    private int cicles = 1;

    GraphView mGraphChart;
    LineGraphSeries<DataPoint> seriesX;
    LineGraphSeries <DataPoint> seriesY;
    LineGraphSeries<DataPoint> seriesZ;
    LineGraphSeries<DataPoint> seriesM;

    SeekBar mSeekBar;

    int mSensorElection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_live_acc);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorElection = SensorManager.SENSOR_DELAY_NORMAL;

        mGraphChart = (GraphView) findViewById(R.id.graphChart);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        mSeekBar.setProgress(0);
        mSeekBar.setMax(3);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress){
                    case 0:
                        Toast.makeText(MainLiveAccelerometer.this, "Set: SENSOR_DELAY_NORMAL", Toast.LENGTH_SHORT).show();
                        mSensorElection = SensorManager.SENSOR_DELAY_NORMAL;
                        break;
                    case 1:
                        Toast.makeText(MainLiveAccelerometer.this, "Set: SENSOR_DELAY_FASTEST", Toast.LENGTH_SHORT).show();
                        mSensorElection = SensorManager.SENSOR_DELAY_FASTEST;
                        break;
                    case 2:
                        Toast.makeText(MainLiveAccelerometer.this, "Set: SENSOR_DELAY_GAME", Toast.LENGTH_SHORT).show();
                        mSensorElection = SensorManager.SENSOR_DELAY_GAME;
                        break;
                    case 3:
                        Toast.makeText(MainLiveAccelerometer.this, "Set: SENSOR_DELAY_UI", Toast.LENGTH_SHORT).show();
                        mSensorElection = SensorManager.SENSOR_DELAY_UI;
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seriesX = new LineGraphSeries<DataPoint>();
        seriesX.setColor(Color.RED);
        seriesX.setTitle("X");
        mGraphChart.addSeries(seriesX);

        seriesY = new LineGraphSeries<DataPoint>();
        seriesY.setColor(Color.GREEN);
        seriesY.setTitle("Y");
        mGraphChart.addSeries(seriesY);

        seriesZ = new LineGraphSeries<DataPoint>();
        seriesZ.setColor(Color.BLUE);
        seriesZ.setTitle("Z");
        mGraphChart.addSeries(seriesZ);

        seriesM = new LineGraphSeries<DataPoint>();
        seriesM.setColor(Color.WHITE);
        seriesM.setTitle("M");
        mGraphChart.addSeries(seriesM);

        mGraphChart.getViewport().setXAxisBoundsManual(true);
        mGraphChart.getLegendRenderer().setVisible(true);
        mGraphChart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        mGraphChart.setBackgroundColor(Color.LTGRAY);
        mGraphChart.getViewport().setMaxX(100);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {


        float x = event.values[X_AXIS_INDEX];
        float y = event.values[1];
        float z = event.values[2];

        Log.d(TAG, "x: " + x + " y: " + y + " z: " + z + "\n");

        seriesX.appendData(new DataPoint(contX, x),false, 1000);
        seriesY.appendData(new DataPoint(contX, y),false, 1000);
        seriesZ.appendData(new DataPoint(contX, z),false, 1000);

        //Acceleration

        //http://stackoverflow.com/questions/4993993/how-to-detect-walking-with-android-accelerometer

        double acceleration = Math.sqrt(pow(x,2) + pow(y,2) + pow(z,2))-9.8;

        seriesM.appendData(new DataPoint(contX, acceleration),false, 1000);

        contX++;
        if (((contX+1)/100)== cicles){
            mGraphChart.getViewport().setMaxX( mGraphChart.getViewport().getMaxX(true)+100);
            cicles++;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, mSensorElection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    //To validate if the Cellphone is on a table
//    public static boolean isStandingStill(float[] accelerationValues) {
//        double acceleration = calculateAcceleration(accelerationValues);
//
//        // If acceleration doesn't differ from earth's gravity more than 10%, then
//        // it's safe to assume that phone is standing still
//        if (acceleration > SensorManager.GRAVITY_EARTH * 0.9
//                && acceleration < SensorManager.GRAVITY_EARTH * 1.1) {
//            return true;
//        }
//        return false;
//    }

}
