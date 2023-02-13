package com.example.placesapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import com.example.placesapp.database.Lokacije;
import com.example.placesapp.database.LokacijeDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.navigation.NavigationView;


import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    EditText lokacija;
    Button button;


    private final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    private TextView city;
    private TextView state;
    private TextView latitude;
    private TextView longitude;
    private TextView address;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            if (Boolean.TRUE.equals(result.get(LOCATION_PERMISSIONS[0]))) {
                startLocationUpdates();
            } else {
                if (shouldShowRequestPermissionRationale(LOCATION_PERMISSIONS[0])) {
                    showPermissionDialog();
                } else {
                    Toast.makeText(MainActivity.this, "Molimo omogućite dozvole u postavkama", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);


        findViewById(R.id.imageMenu).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);

                LokacijeDatabase db = Room.databaseBuilder(getApplicationContext(),
                        LokacijeDatabase.class, "lokacije-database").allowMainThreadQueries().build();

                List<Lokacije> lokacijeLista = db.lokacijaDAO().getAllLokacije();

                ListView lv = (ListView) findViewById(R.id.list);
                String[] lv_arr = new String[lokacijeLista.size()];

                for (int i = 0; i < lokacijeLista.size(); i++) {
                    lv_arr[i] = (lokacijeLista.get(i)).name;
                }

                lv.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, lv_arr));

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Lokacije lista = lokacijeLista.get(position);
                        Intent intent = new Intent(MainActivity.this, PlaceActivity.class);
                        intent.putExtra("name", lista.name);
                        intent.putExtra("id", lista.id);
                        startActivity(intent);
                    }
                });
            }


        });


        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        address = findViewById(R.id.address);
        lokacija = findViewById(R.id.dodajlokaciju);
        button = findViewById(R.id.button);


        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
                lokacija.setText("");
            }
        });

        locationRequest = new LocationRequest.Builder(1000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(10)
                .build();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                StringBuilder sb = new StringBuilder();

                latitude.setText("Latitude: " + String.format("%.6f °", location.getLatitude()));
                longitude.setText("Longtitude: " + String.format("%.6f °", location.getLongitude()));
                address.setText("Address: \n" + String.format(addresses.get(0).getAddressLine(0)));
                city.setText(String.format(addresses.get(0).getLocality()));
                state.setText(String.format(addresses.get(0).getCountryName()));

            }
        };

        checkAndRequestPermissions();
    }


    private void saveData() {
        String ime = lokacija.getText().toString().trim();
        if (ime.matches("")) {
            Toast.makeText(this, "Molimo Vas unesite lokaciju", Toast.LENGTH_SHORT).show();
            return;

        } else {

            Lokacije lokacija = new Lokacije(ime);
            LokacijeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    LokacijeDatabase.class, "lokacije-database").allowMainThreadQueries().build();

            db.lokacijaDAO().insertAll(lokacija);
            Toast.makeText(this, "Lokacija uspješno spremljena", Toast.LENGTH_SHORT).show();
        }
    }


    public void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, LOCATION_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            requestPermissionLauncher.launch(LOCATION_PERMISSIONS);
        }
    }

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Potrebna dozvola")
                .setMessage("Ne možemo precizno odrediti vašu lokaciju bez adekvatne dozvole")
                .setPositiveButton("Razumijem", (dialog, which) -> {
                    requestPermissionLauncher.launch(LOCATION_PERMISSIONS);
                    dialog.dismiss();
                });

        AlertDialog alertDialog = builder.create();
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}