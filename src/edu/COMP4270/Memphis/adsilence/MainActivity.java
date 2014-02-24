package edu.COMP4270.Memphis.adsilence;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
 
public class MainActivity extends Activity {
	
	final String TAG = "LightSensorDemo";
	final String TAG2 = "LightSensorDemo_Error";
	ProgressBar lightMeter;
	TextView textMax;
	TextView textReading;
	//File dataFile;
	File dataDir;
	FileOutputStream outStream;
	BufferedOutputStream out;
	long startTime;
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lightMeter = (ProgressBar)findViewById(R.id.lightmeter);
        textMax = (TextView)findViewById(R.id.max);
        textReading = (TextView)findViewById(R.id.reading);
         
        SensorManager sensorManager
        = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor
        = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
      
        if(isExternalStorageWritable()) {
        	Log.i(TAG2, "External Storage Available");
        	Toast.makeText(MainActivity.this, "External storage available", Toast.LENGTH_LONG).show();
        	
        	dataDir = getDataStorageDir("lightData");
        	
        	// Create BufferedOutputStream
        	out = getFileOutputStream(dataDir, "lightData.txt");
    	}
        else {
        	Log.i(TAG2, "External Storage Unavailable");
        	Toast.makeText(MainActivity.this, "External storage unavailable", Toast.LENGTH_LONG).show();
        }
        
        // Light sensor init
        if (lightSensor == null){
         Toast.makeText(MainActivity.this,
           "No Light Sensor! quit-",
           Toast.LENGTH_LONG).show();
        }else{
         float max =  lightSensor.getMaximumRange();
         lightMeter.setMax((int)max);
         textMax.setText("Max Reading: " + String.valueOf(max));
         
         startTime = System.currentTimeMillis();
         
         sensorManager.registerListener(lightSensorEventListener,
           lightSensor,
           SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
     
    
    SensorEventListener lightSensorEventListener = new SensorEventListener(){
    	@Override
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    		// TODO Auto-generated method stub
    		Toast.makeText(MainActivity.this, "" + accuracy, Toast.LENGTH_LONG).show();
    	}
 
    	@Override
    	public void onSensorChanged(SensorEvent event) {
    		// TODO Auto-generated method stub
    		if(event.sensor.getType()==Sensor.TYPE_LIGHT){
    			float currentReading = event.values[0];
    			lightMeter.setProgress((int)currentReading);
    			
    			// Use this to simply log sensor data to LogCat
    			String currentReadingStr = String.valueOf(currentReading);
    			//Log.i(TAG, currentReadingStr);
    			long currentTime = System.currentTimeMillis() - startTime;
    			String elapsedTime = "" + currentTime/1000 + ":" + currentTime%1000;
    			
    			String output =  "\n" + elapsedTime + ", " + String.valueOf(currentReading);
    			writeData(output);
    			textReading.setText("Current Reading: " + currentReadingStr);
    			//textReading.setText("Current Reading: " + String.valueOf(currentReading));
    		}
    	}
    };
    
    
    @Override
    public void onDestroy(){
    	// Closing file stream
    	// This should be done elsewhere (onPause(), onStop()) because onDestroy() is not
    	// 		guaranteed to be called.
    	// The lightsensor data gathering should take place in a separate, long-running
    	//		service and thread.
    	// TODO: If this is a bad place for out.close() then its a TERRIBLE place for out.flush()
    	// TODO: Should be moved to onPause(), onStop() to ensure data in buffer is written to file
    	try {
    		out.flush();
    		out.close();
		} catch (IOException e) {
			Log.e(TAG2, "ERROR: IO EXCEPTION");
		}
    }
    
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    
    // Get storage directory and file
    // Takes File (directory name)
    public File getDataStorageDir(String dirName) {
    	
    	// get the path to sdcard
    	File sdcard = Environment.getExternalStorageDirectory();
    	
    	// to this path add a new directory path
    	File dir = new File(sdcard.getAbsolutePath() + "/" + dirName + "/");
    	
    	// create this directory if not already created
    	dir.mkdir();
    	
    	return dir;
    }
    
    // Get buffered output stream
    // Takes File (directory), File (file name)
    BufferedOutputStream getFileOutputStream(File dataDirectory, String dataFileName){
    	
    	FileOutputStream unbufferedOutputStream = null;
    	BufferedOutputStream bufferedOutputStream = null;
    	
    	File dataFile = new File(dataDir, dataFileName);
    	
    	try {
    		unbufferedOutputStream = new FileOutputStream(dataFile);
			bufferedOutputStream = new BufferedOutputStream(unbufferedOutputStream);
		} catch (FileNotFoundException e) {
			Log.e(TAG2, "ERROR: FILE NOT FOUND");
		}
    	
    	return bufferedOutputStream;
    	/*
		try {
			os = new FileOutputStream(dataFile);
			//String data = "This is the content of my file";
        	//outStream.write(data.getBytes());
        	//outStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Toast.makeText(MainActivity.this, "ERROR: FILE NOT FOUND", Toast.LENGTH_LONG).show();
			Log.e(TAG2, "ERROR: FILE NOT FOUND");
		} catch (IOException e) {
			Toast.makeText(MainActivity.this, "ERROR: I/O EXCEPTION", Toast.LENGTH_LONG).show();
			Log.e(TAG2, "ERROR: I/O EXCEPTION");
		}
		*/
    }	
    
    
    public void writeData(String data){
    	try {
			out.write(data.getBytes());
		} catch (IOException e) {
			Log.e(TAG2, "ERROR: IO EXCEPTION, UNABLE TO WRITE DATA");
		}
    }
}
