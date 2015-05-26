package com.example.usuario.sensors;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private final double THRESHOLDEDBED = 2;
    private final double THRESHOLDEDWALK = 5;
    private final double THRESHOLDEDRUN = 20;

    GraphView mGraphChart;
    LineGraphSeries<DataPoint> seriesFFT;
    LineGraphSeries<DataPoint> seriesFFTY;
    LineGraphSeries<DataPoint> seriesFFTM;

    SeekBar mSeekBar;
    Button mIdentifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fft);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mIdentifyButton = (Button) findViewById(R.id.identifyButton);

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


                    if (cicles < N) {
                        Toast.makeText(MainFFT.this, "Waiting for more samples. Try a lower value for the moment. It may take some seconds.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        N = (int) Math.pow(2, mSeekBar.getProgress());
                        Toast.makeText(MainFFT.this, "N=" + N, Toast.LENGTH_SHORT).show();
                        FFT fft = new FFT(N);
                        double[] x = new double[N];
                        double[] y = new double[N];

                        for (int j = 0; j < N; j++) {
                            x[j] = mInput[j];
                            y[j] = 0;
                        }

                        fft.fft(x, y);

                        mGraphChart.removeAllSeries();
                        mGraphChart.addSeries(seriesFFT);
                        mGraphChart.addSeries(seriesFFTY);
                        mGraphChart.addSeries(seriesFFTM);
                        mGraphChart.getViewport().setMaxX(N);

                        DataPoint[] values = new DataPoint[N];
                        DataPoint[] valuesY = new DataPoint[N];
                        DataPoint[] valuesM = new DataPoint[N];

                        double sumMagnitud = 0;
                        for (int j = 0; j < N; j++) {
                            Log.d(TAG, "X=" + x[j] + "Y=" + y[j]);
                            DataPoint vX = new DataPoint(j, x[j]);
                            values[j] = vX;

                            DataPoint vy = new DataPoint(j, y[j]);
                            valuesY[j] = vy;

                            //Calculate value Magnitud

                            double magnitud = Math.sqrt(Math.pow(x[j], 2) + Math.pow(y[j], 2));
                            sumMagnitud += magnitud;
                            DataPoint vm = new DataPoint(j, magnitud);
                            valuesM[j] = vm;


                        }

                        Log.d(TAG, "SumMagnitud: " + sumMagnitud / N);

                        seriesFFT.resetData(values);
                        seriesFFTY.resetData(valuesY);
                        seriesFFTM.resetData(valuesM);

                        double amplitud = sumMagnitud/N;

                        //Notifications taken from http://javatechig.com/android/android-notification-example-using-notificationcompat

                        // Use NotificationCompat.Builder to set up our notification.
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainFFT.this);

                        //icon appears in device notification bar and right hand corner of notification
                        builder.setSmallIcon(R.mipmap.ic_launcher);

                        // This intent is fired when notification is clicked
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        PendingIntent pendingIntent = PendingIntent.getActivity(MainFFT.this, 0, intent, 0);

                        // Set the intent that will fire when the user taps the notification.
                        builder.setContentIntent(pendingIntent);

                        // Large icon appears on the left of the notification
                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

                        if (amplitud<THRESHOLDEDBED){



                            // Content title, which appears in large type at the top of the notification
                            builder.setContentTitle("Activity on bed");

                            // Content text, which appears in smaller text below the title
                            builder.setContentText("Your cellphone is lying on the bed.");

                            // The subtext, which appears under the text on newer devices.
                            // This will show-up in the devices with Android 4.2 and above only

                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            // Will display the notification in the notification bar
                            notificationManager.notify(0, builder.build());
                        }
                        else if (amplitud >= THRESHOLDEDWALK && amplitud < THRESHOLDEDRUN){
                            // Content title, which appears in large type at the top of the notification
                            builder.setContentTitle("Activity walking");

                            // Content text, which appears in smaller text below the title
                            builder.setContentText("You are walking");

                            // The subtext, which appears under the text on newer devices.
                            // This will show-up in the devices with Android 4.2 and above only

                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            // Will display the notification in the notification bar
                            notificationManager.notify(0, builder.build());
                        }
                        else if (amplitud > THRESHOLDEDRUN){
                            // Content title, which appears in large type at the top of the notification
                            builder.setContentTitle("Activity running");

                            // Content text, which appears in smaller text below the title
                            builder.setContentText("You are running");

                            // The subtext, which appears under the text on newer devices.
                            // This will show-up in the devices with Android 4.2 and above only

                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            // Will display the notification in the notification bar
                            notificationManager.notify(0, builder.build());
                        }
                        else{
                            // Content title, which appears in large type at the top of the notification
                            builder.setContentTitle("Activity not recognized");

                            // Content text, which appears in smaller text below the title
                            builder.setContentText("Try again. You may need to choose a higher N.");

                            // The subtext, which appears under the text on newer devices.
                            // This will show-up in the devices with Android 4.2 and above only

                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            // Will display the notification in the notification bar
                            notificationManager.notify(0, builder.build());
                        }

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mIdentifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cicles = 0;
                mGraphChart.removeAllSeries();
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
