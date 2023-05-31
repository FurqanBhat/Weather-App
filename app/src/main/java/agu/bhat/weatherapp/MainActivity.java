package agu.bhat.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.se.omapi.SEService;
import android.se.omapi.SEService.OnConnectedListener;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
    private static final int REQUEST_FINE_LOCATION = 9;
    RequestQueue requestQueue;
    TextView tvCityName, tvMaxTemp, tvMinTemp, tvTemp;
    ImageView ivIcon, ivSearch;
    EditText etCityName;
    double lat = 0.0, lon = 0.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    Location lastLocation;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    LocationManager locationManager;


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
        Location myLocation = getLastLocation();
        lastLocation=getLastLocation();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        updateLocation();
        createLocationRequest();
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                lastLocation = locationResult.getLastLocation();


                lat = lastLocation.getLatitude();
                lon = lastLocation.getLongitude();
                getFeed();

            }
        };


        requestQueue = Volley.newRequestQueue(this);
        getFeed();


        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocation();
            }
        });


    }

    private Location getLastLocation() {
        locationManager = (LocationManager) MainActivity.this.getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            Location l = locationManager.getLastKnownLocation(provider);
            if(l==null){
                continue;
            }
            bestLocation=l;
        }
        return bestLocation;
    }

    public void updateLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    lastLocation=location;
                        lat=lastLocation.getLatitude();
                        lon=lastLocation.getLongitude();

                }
            });
        }
    }


    protected void createLocationRequest() {
        locationRequest=new LocationRequest();
        locationRequest.setInterval(3*1000);
        locationRequest.setFastestInterval(2*1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    }

    public void getFeed(){
        if(lastLocation!=null) {
            lat = lastLocation.getLatitude();
            lon = lastLocation.getLongitude();
        }
        String url="https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid=44954eeb8e6637b185893220202e5570";
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET,url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(lastLocation!=null) {
                    setUiValues(response);
                }

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
        JSONObject mainObject, cloudObject;
        String cityName;
        double maxTemp=0.0, minTemp=0.0, temp=0.0;
        int cloudy=0;

        try {
            mainObject=response.getJSONObject("main");
            cloudObject=response.getJSONObject("clouds");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            temp=mainObject.getDouble("temp");
            minTemp=mainObject.getDouble("temp_min");
            maxTemp=mainObject.getDouble("temp_max");
            cloudy=cloudObject.getInt("all");
            cityName=response.getString("name");

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if(cloudy>30 && cloudy<70){
            ivIcon.setImageResource(R.drawable.cloudy);
        }
        else if(cloudy>70){
            ivIcon.setImageResource(R.drawable.raining);
        }
        else{
            ivIcon.setImageResource(R.drawable.sunny);
        }

            tvCityName.setText(cityName);
            tvTemp.setText(temp + "");
            tvMaxTemp.setText(maxTemp + "");
            tvMinTemp.setText(minTemp + "");
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocation();

            }
        }
    }



//    private boolean locationPermissions(){
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)==
//                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
//                PackageManager.PERMISSION_GRANTED;
//    }

}