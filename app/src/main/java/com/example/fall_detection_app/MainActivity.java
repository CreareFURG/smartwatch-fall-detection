package com.example.fall_detection_app;

import android.app.Activity;
import android.app.BackgroundServiceStartNotAllowedException;
import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.fall_detection_app.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Executor;

import javax.security.auth.login.LoginException;

public class MainActivity extends Activity {

    private Button button;
    private ActivityMainBinding binding;
    public float accX, accY, accZ, gyrX, gyrY, gyrZ; // Accelerometer and Gyroscope values
    public float gravity; // Gravity force
    public float gyrMagnitude; // Gyroscope magnitude
    public int output = 0;

    public int dataIntervalTime = 50; // Data collection interval time in milliseconds
    File filepath; // File path for storing data
    FileOutputStream fileOutputStream; // File output stream for writing data to file

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep screen on

        writeHeader(); // Write header to file
        SensorThread sensorThread = new SensorThread(); // Create a new thread for sensor data collection
        sensorThread.start();
        FileTread fileTread = new FileTread(); // Create a new thread for writing data to file
        fileTread.start();
    }

    public void writeHeader(){
        DateFormat dateFormatLog = new SimpleDateFormat("'D'dd_MM_yyyy'_T'HH_mm_ss");
        String logTime = dateFormatLog.format(Calendar.getInstance().getTime());
        String header = "dateTime, accX, accY, accZ, gyrX, gyrY, gyrZ, gravity, gyrMagnitude, output" + "\n";
        File root = new File(Environment.getStorageDirectory(), "emulated/0/Documents/FallDetectionApp");
        if (!root.exists()) {
            root.mkdir();
        }
        filepath = new File(root, "Dataset_" + dataIntervalTime + "ms_" + logTime + ".csv");
        try {
            fileOutputStream = new FileOutputStream(filepath, true);
            // Adiciona apenas uma vez o cabeçalho no arquivo, o qual é armazenado em memória
            fileOutputStream.write(header.getBytes(StandardCharsets.US_ASCII));
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class FileTread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(dataIntervalTime);
                    writeDataToFile();
                    button = findViewById(R.id.button);
                    button.setOnClickListener(v -> {
                        if (output == 0) {
                            output = 1;
                            Log.e("Output", String.valueOf(output));
                            button.setText("Finalizar queda");
                        } else {
                            output = 0;
                            Log.e("Output", String.valueOf(output));
                            button.setText("Iniciar queda");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        public void writeDataToFile(){
            DateFormat dateFormatLog = new SimpleDateFormat("dd-MM-yy'T'HH:mm:ss.SSSS");
            String timeValue = dateFormatLog.format(Calendar.getInstance().getTime());
            String data = timeValue + ", " + accX + ", " + accY + ", " + accZ + ", " + gyrX + ", " + gyrY + ", " + gyrZ + ", " + gravity + ", " + gyrMagnitude + ", " + output + "\n";
            try {
                fileOutputStream.write(data.getBytes(StandardCharsets.US_ASCII));
                fileOutputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Thread for reading sensor values
    private class SensorThread extends Thread implements SensorEventListener {
        @Override
        public void run() {
            registerSensors();
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // Armazenando valores dos eventos do ACELERÔMETRO
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                accX = event.values[0];
                accY = event.values[1];
                accZ = event.values[2];
                gravity = (float) Math.sqrt(accX * accX + accY * accY + accZ * accZ);
            }

            // Armazenando valores dos eventos do GIROSCÓPIO
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                gyrX = event.values[0];
                gyrY = event.values[1];
                gyrZ = event.values[2];
                gyrMagnitude = (float) Math.sqrt(gyrX * gyrX + gyrY * gyrY + gyrZ * gyrZ);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        public void registerSensors() {
            SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            Sensor acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_FASTEST);
            Sensor gyr = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, gyr, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }
}