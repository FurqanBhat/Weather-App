package agu.bhat.weatherapp;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.se.omapi.SEService;
import android.se.omapi.SEService.OnConnectedListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    RequestQueue requestQueue;
    TextView tvCityName, tvMaxTemp, tvMinTemp, tvTemp, tvPrecepProbab, tvPrecepHours;
    ImageView ivIcon, ivSearch;
    EditText etCityName;
    double cityLat=0, cityLon=0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvCityName = findViewById(R.id.tvCityName);
        tvMinTemp = findViewById(R.id.tvMinTemp);
        tvMaxTemp = findViewById(R.id.tvMaxTemp);
        tvTemp = findViewById(R.id.tvTemp);
        ivSearch = findViewById(R.id.ivSearch);
        ivIcon = findViewById(R.id.ivIcon);
        etCityName=findViewById(R.id.etCityName);
        tvPrecepHours=findViewById(R.id.tvPrecepHours);
        tvPrecepProbab=findViewById(R.id.tvPrecipProbab);


        requestQueue = Volley.newRequestQueue(this);

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city=etCityName.getText().toString().trim();
                getCityLocation(city);
            }
        });

    }













    public void getFeed(double lat, double lon){
        String url="https://api.open-meteo.com/v1/forecast?latitude="+lat+"&longitude="+lon+"&hourly=temperature_2m,cloudcover,visibility,is_day&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_hours,precipitation_probability_max&forecast_days=1&timezone=auto";


        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET,url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                    setUiValues(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Problem OCCURED", Toast.LENGTH_SHORT).show();

            }
        });
        requestQueue.add(request);
    }






    protected void setUiValues(JSONObject response){
        JSONObject hourly, daily;
        double maxTemp=0.0, minTemp=0.0, temp=0.0, precipitationHours=0.0;
        int cloudy=0, isDay=0, precipitationProbability=0;

        try {
            hourly=response.getJSONObject("hourly");
            daily=response.getJSONObject("daily");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            temp=hourly.getJSONArray("temperature_2m").getDouble(0);
            isDay=hourly.getJSONArray("is_day").getInt(0);
            cloudy=hourly.getJSONArray("cloudcover").getInt(0);
            minTemp= (double) daily.getJSONArray("temperature_2m_min").getDouble(0);
            maxTemp= (double) daily.getJSONArray("temperature_2m_max").getDouble(0);
            precipitationHours=daily.getJSONArray("precipitation_hours").getDouble(0);
            precipitationProbability=daily.getJSONArray("precipitation_probability_max").getInt(0);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if(cloudy>30 && cloudy<70 && isDay==1){
            ivIcon.setImageResource(R.drawable.halfcloudy);
        }
        else if(cloudy>70){
            ivIcon.setImageResource(R.drawable.fullcloudy);
        }
        else if(cloudy<30 && isDay==1){
            ivIcon.setImageResource(R.drawable.sunny);
        }
        else{
            ivIcon.setImageResource(R.drawable.night);
        }
            tvPrecepProbab.setText(precipitationProbability+"%");
            tvPrecepHours.setText(precipitationHours+" hrs");
            tvMaxTemp.setText(maxTemp+"°C");
            tvTemp.setText(temp + "°C");
            tvMinTemp.setText(minTemp + "°C");
        }






    private void getCityLocation(String name){
        tvCityName.setText(name.toUpperCase());
        String url="https://api.openweathermap.org/geo/1.0/direct?q="+name+"&limit=1&appid=44954eeb8e6637b185893220202e5570";
        JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            public void onResponse(JSONArray response) {

                try {
                    JSONObject object= (JSONObject) response.get(0);
                    cityLat=object.getDouble("lat");
                    cityLon=object.getDouble("lon");
                    getFeed(cityLat, cityLon);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "ERROR OCCURED IN GETCITYLOCATION", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(request);

    }



}
