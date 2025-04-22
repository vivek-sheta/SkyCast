package com.example.skycast.ui.App;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skycast.MapsFragment;
import com.example.skycast.R;

public class MapPickerHostActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_host_activity);

        // Add the fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new MapsFragment())
                .commit();
    }
}