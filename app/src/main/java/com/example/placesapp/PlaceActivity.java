package com.example.placesapp;

import android.Manifest;

import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.placesapp.database.Lokacije;
import com.example.placesapp.database.LokacijeDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class PlaceActivity extends AppCompatActivity {

    EditText lokacija;


    private final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private TextView city;

    private TextView latitude;
    private TextView longitude;

    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        Intent intent = getIntent();
        String grad = intent.getStringExtra("name");
        int id = intent.getIntExtra("id", 0);


        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        queue = Volley.newRequestQueue(this);
        String url = "https://nominatim.openstreetmap.org/search?city=" + grad + "&format=json";


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            JSONObject jresponse = response.getJSONObject(0);
                            city.setText(jresponse.getString("display_name"));
                            latitude.setText("Latitude: " + jresponse.getString("lat") + " °");
                            longitude.setText("Longtitude: " + jresponse.getString("lon") + " °");

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
            }
        }

        );

        queue.add(jsonArrayRequest);


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

                lv.setAdapter(new ArrayAdapter<String>(PlaceActivity.this,
                        android.R.layout.simple_list_item_1, lv_arr));

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Lokacije lista = lokacijeLista.get(position);
                        Intent intent = new Intent(PlaceActivity.this, PlaceActivity.class);
                        intent.putExtra("name", lista.name);
                        startActivity(intent);
                    }
                });
            }

        });

        city = findViewById(R.id.city);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);


        findViewById(R.id.delete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LokacijeDatabase db = Room.databaseBuilder(getApplicationContext(),
                        LokacijeDatabase.class, "lokacije-database").allowMainThreadQueries().build();

                db.lokacijaDAO().delete(id);
                Toast.makeText(PlaceActivity.this, "Uspješno ste izbrisali lokaciju", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PlaceActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PlaceActivity.this, MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}