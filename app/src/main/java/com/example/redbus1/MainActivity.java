package com.example.redbus1;

//package com.example.redbus;

//package com.example.redbus;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;


    private TextView xAxis, yAxis, zAxis;
    private TextView gyroX, gyroY, gyroZ;
    private TextView magX, magY, magZ;


    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initialize SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Get accelerometer sensor
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Get gyroscope sensor
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Get magnetometer sensor
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        // Initialize TextViews
        xAxis = (TextView) findViewById(R.id.xAxis);
        yAxis = (TextView) findViewById(R.id.yAxis);
        zAxis = (TextView) findViewById(R.id.zAxis);

        gyroX = (TextView) findViewById(R.id.gyroX);
        gyroY = (TextView) findViewById(R.id.gyroY);
        gyroZ = (TextView) findViewById(R.id.gyroZ);

        magX = (TextView) findViewById(R.id.magX);
        magY = (TextView) findViewById(R.id.magY);
        magZ = (TextView) findViewById(R.id.magZ);



    }
    private static final float ALPHA = 0.9f; // the smoothing factor

    private float[] gravity = new float[3]; // gravity values
    private boolean gravitySet = false;
    private static final float THRESHOLD = 0.1f; // m/s^2

    private static float smoothedValue = 0.0F;
    private static final float currentValue = 10.0F;
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get sensor type
        int sensorType = event.sensor.getType();

        // Get sensor values
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Smooth the accelerometer values with a low-pass filter
            if (!gravitySet) {
                // Initialize the gravity values with the first accelerometer readings
                gravity[0] = x;
                gravity[1] = y;
                gravity[2] = z;
                gravitySet = true;
            } else {
                // Apply the low-pass filter to smooth out the readings
                gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
                gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
                gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;
            }

            // Subtract the gravity component from the accelerometer readings to get the linear acceleration
            float linearX = x - gravity[0];
            float linearY = y - gravity[1];
            float linearZ = z - gravity[2];

            // Update accelerometer TextViews
            if (Math.abs(linearX) < 0.1f) {
                linearX = 0f;
            }
            if (Math.abs(linearY) < 0.1f) {
                linearY = 0f;
            }
            if (Math.abs(linearZ) < 0.1f) {
                linearZ = 0f;
            }
            smoothedValue = ALPHA * smoothedValue + (1 - ALPHA) * currentValue;

            xAxis.setText("X-axis: " + linearX);
            yAxis.setText("Y-axis: " + linearY);
            zAxis.setText("Z-axis: " + linearZ);
        }
        else if (sensorType == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Check if the gyroscope readings are below the threshold value
            if (Math.abs(x) < THRESHOLD) {
                x = 0.0f;
            }
            if (Math.abs(y) < THRESHOLD) {
                y = 0.0f;
            }
            if (Math.abs(z) < THRESHOLD) {
                z = 0.0f;
            }

            // Update gyroscope TextViews
            gyroX.setText("X-axis: " + x);
            gyroY.setText("Y-axis: " + y);
            gyroZ.setText("Z-axis: " + z);
        } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Update magnetometer TextViews
            magX.setText("X-axis: " + x);
            magY.setText("Y-axis: " + y);
            magZ.setText("Z-axis: " + z);
        }
        String Acc=xAxis.getText().toString()+" "+yAxis.getText().toString()+" "+zAxis.getText().toString();
        String gyro=gyroX.getText().toString()+" "+gyroY.getText().toString()+" "+gyroZ.getText().toString();
        String mag=magX.getText().toString()+" "+magY.getText().toString()+" "+magZ.getText().toString();
        HashMap<String,Object> map=new HashMap<>();
        map.put("Acceleromter",Acc);
        map.put("Gyrometer",gyro);
        map.put("Magnetometer",mag);
        FirebaseDatabase.getInstance().getReference().child("Sensors").setValue(map);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String sensorName = sensor.getName();
        String accuracyLevel;

        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyLevel = "high";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyLevel = "medium";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyLevel = "low";
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                accuracyLevel = "unreliable";
                break;
            default:
                accuracyLevel = "unknown";
                break;
        }

        Log.d("SensorAccuracy", sensorName + " accuracy changed to " + accuracyLevel);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listeners
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listeners to save battery
        sensorManager.unregisterListener(this);
    }
}