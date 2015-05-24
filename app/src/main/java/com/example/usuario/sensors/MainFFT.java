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


public class MainFFT extends ActionBarActivity implements SensorEventListener {

    private static final String TAG = MainLiveAccelerometer.class.getSimpleName();

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private int contX = 0;
    private int cicles = 0;
    private int stepSize = 2;
    private double[] mInput;
    private int N;

    GraphView mGraphChart;
    LineGraphSeries<DataPoint> seriesFFT;
    LineGraphSeries<DataPoint> seriesFFTY;
    LineGraphSeries<DataPoint> seriesFFTM;

    SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fft);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mGraphChart = (GraphView) findViewById(R.id.graphChart);

        seriesFFT = new LineGraphSeries<DataPoint>();
        seriesFFT.setColor(Color.RED);
        seriesFFT.setTitle("X");

        seriesFFTY = new LineGraphSeries<DataPoint>();
        seriesFFTY.setColor(Color.BLUE);
        seriesFFTY.setTitle("Y");

        seriesFFTM = new LineGraphSeries<DataPoint>();
        seriesFFTM.setColor(Color.GREEN);
        seriesFFTM.setTitle("M");

        mGraphChart.addSeries(seriesFFT);
        mGraphChart.addSeries(seriesFFTY);
        mGraphChart.addSeries(seriesFFTM);


        mGraphChart.getViewport().setXAxisBoundsManual(true);
        mGraphChart.getLegendRenderer().setVisible(true);
        mGraphChart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        mGraphChart.setBackgroundColor(Color.LTGRAY);
        mGraphChart.getViewport().setMaxX(100);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setProgress(0);
        mSeekBar.setMax(10);
        mInput = new double[1024];
        N=0;

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mSeekBar.getProgress()==0){
                    Toast.makeText(MainFFT.this, "Try another value.", Toast.LENGTH_SHORT).show();
                }
                else {


                    N = (int) Math.pow(2,mSeekBar.getProgress());
                    Toast.makeText(MainFFT.this, "N="+N,Toast.LENGTH_SHORT).show();
                    FFT fft = new FFT(N);
                    double[] x = new double[N];
                    double[] y = new double[N];

                    for (int j = 0; j < N; j++) {
                        x[j] = mInput[j];
                        y[j] = 0;
                    }

                    Log.d(TAG, "X="+x[0]);
                    Log.d(TAG, "X="+x[1]);

                    fft.fft(x, y);

                    mGraphChart.removeAllSeries();
                    mGraphChart.addSeries(seriesFFT);
                    mGraphChart.addSeries(seriesFFTY);
                    mGraphChart.addSeries(seriesFFTM);
                    mGraphChart.getViewport().setMaxX(N);

                    DataPoint[] values = new DataPoint[N];
                    DataPoint[] valuesY = new DataPoint[N];
                    DataPoint[] valuesM = new DataPoint[N];
                    for (int j = 0; j < N ; j++){
                        Log.d(TAG, "X="+x[j] + "Y="+y[j]);
                        DataPoint v = new DataPoint(j,x[j]);
                        values[j] = v;

                        DataPoint vy = new DataPoint(j,y[j]);
                        valuesY[j] = vy;

                        //Calculate value Magnitud

                        double magnitud = Math.sqrt(Math.pow(x[j],2)+Math.pow(y[j],2));

                        DataPoint vm = new DataPoint(j,magnitud);
                        valuesM[j] = vm;
                    }

                    seriesFFT.resetData(values);
                    seriesFFTY.resetData(valuesY);
                    seriesFFTM.resetData(valuesM);




                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        //Log.d(TAG, "x: " + x + " y: " + y + " z: " + z + "\n");

        //Acceleration

        //http://stackoverflow.com/questions/4993993/how-to-detect-walking-with-android-accelerometer

        double acceleration = Math.sqrt(pow(x,2) + pow(y,2) + pow(z,2))-9.8;
        if (cicles < 1024){
            mInput[cicles] = acceleration;
            cicles++;
        }




        //seriesFFT.appendData(new DataPoint(contX, acceleration),false, 1000);

//        contX++;
//        if (((contX+1)/100)== cicles){
//            mGraphChart.getViewport().setMaxX( mGraphChart.getViewport().getMaxX(true)+100);
//            cicles++;
//        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
