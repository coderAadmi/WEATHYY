package com.prady.imagedownloader;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


class WeatherResult extends ViewModel{
    String result;
}

public class MainActivity extends AppCompatActivity {
    EditText editText;
    ImageDownloader task;
    TextView desc;

    WeatherResult weatherResult;

    public class ImageDownloader extends AsyncTask<String,Integer,JSONObject>{
        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    //Log.d("POP", "Connected....");
                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    int data = reader.read();
                    String result = "";
                    while (data != -1) {
                        result += (char) data;
                        data = reader.read();
                    }

                    JSONObject object = new JSONObject(result);
                    //Log.i("POP", result);
                    return object;

                }
                return null;
            }catch (Exception e) {
                //Log.d("EXEC",e.toString());
                Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Not a valid city..",Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
                //e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("JSON","POOP");

        editText = findViewById(R.id.editText);
        desc = findViewById(R.id.textView2);

       // ViewModelProviders.of(this).get(WeatherResult.class);

    }

    public void show(View v) {
        desc.setVisibility(View.INVISIBLE);
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        String city = editText.getText().toString();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            Log.d("CONN","True");
            try {
                task = new ImageDownloader();
                JSONObject object = task.execute("https://openweathermap.org/data/2.5/weather?q=" + city + "&appid=b6907d289e10d714a6e88b30761fae22").get();
                if (object != null)
                {
                    String result;
                    JSONArray weather = object.getJSONArray("weather");
                    String _weather = weather.getJSONObject(0).getString("main");
                    JSONObject main = object.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    float humidity = (float) main.getDouble("humidity");
                    desc.setVisibility(View.VISIBLE);
                    result = "weather: " + _weather + "\nTemperature: " + (float) temperature + "\nHumidity: " + humidity;
                    desc.setText(result);
                }
            }catch (Exception e) {
                //e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find weather..", Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            Toast.makeText(getApplicationContext(),"NO INTERNET CONNECTION..",Toast.LENGTH_LONG).show();
        }
    }


}