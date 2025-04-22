package com.example.skycast.ui.App;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.skycast.MapsFragment;
import com.example.skycast.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";
    private static final int REQUEST_MAP_LOCATION = 101;

    private EditText etCity;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        etCity = findViewById(R.id.etCity);
        etCity.setText("Calgary"); // Default city

        // Manual location update
        findViewById(R.id.btnUpdateLocationText).setOnClickListener(v -> updateLocation());

        // Map location picker
        findViewById(R.id.btnUpdateLocation).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerHostActivity.class);
            startActivityForResult(intent, REQUEST_MAP_LOCATION);
        });

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> logoutUser());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MAP_LOCATION) {
            if (resultCode == RESULT_OK && data != null) {
                handleLocationResult(data);
            } else {
                Log.d(TAG, "Map selection canceled");
            }
        }
    }

    private void handleLocationResult(Intent data) {
        if (data.hasExtra("CITY_NAME")) {
            String cityName = data.getStringExtra("CITY_NAME");
            etCity.setText(cityName);
            returnResultToParent(cityName);
            Toast.makeText(this, getString(R.string.location_updated, cityName), Toast.LENGTH_SHORT).show();
        }
        else if (data.hasExtra("lat") && data.hasExtra("lng")) {
            String coords = "Lat: " + data.getDoubleExtra("lat", 0) +
                    ", Lng: " + data.getDoubleExtra("lng", 0);
            etCity.setText(coords);
            returnResultToParent(coords);
            Toast.makeText(this, getString(R.string.location_updated, coords), Toast.LENGTH_SHORT).show();
        }
    }

    private void returnResultToParent(String location) {
        Intent result = new Intent();
        result.putExtra("CITY_NAME", location);
        setResult(RESULT_OK, result);
    }

    private void updateLocation() {
        String city = etCity.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_city, Toast.LENGTH_SHORT).show();
            return;
        }
        returnResultToParent(city);
        finish();
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(this, AuthActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}