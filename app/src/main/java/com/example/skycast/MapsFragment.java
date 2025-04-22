package com.example.skycast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsFragment extends Fragment {

    private GoogleMap mMap;
    private Marker selectedMarker;
    private LatLng selectedLocation;
    private Button btnConfirm;
    private Geocoder geocoder;
    private static final long GEOCODING_TIMEOUT_MS = 15000; // 15 seconds timeout
    private Handler geocodingHandler;
    private boolean isGeocodingComplete = false;

    private final Runnable geocodingTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isGeocodingComplete) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                            "Location lookup timed out",
                            Toast.LENGTH_SHORT).show();
                    returnCityName("Calgary");
                });
            }
        }
    };

    @Override
    public void onDestroyView() {
        // Clean up handler to prevent memory leaks
        if (geocodingHandler != null) {
            geocodingHandler.removeCallbacks(geocodingTimeoutRunnable);
        }
        super.onDestroyView();
    }
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            geocoder = new Geocoder(requireContext(), Locale.getDefault());

            // Default view over Calgary
            LatLng calgary = new LatLng(51.0447, -114.0719);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(calgary, 10f));

            mMap.setOnMapClickListener(latLng -> {
                selectedLocation = latLng;
                updateMarker(latLng);
                btnConfirm.setVisibility(View.VISIBLE);
            });
        }
    };

    private void updateMarker(LatLng latLng) {
        if (selectedMarker != null) {
            selectedMarker.remove();
        }
        selectedMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title("Selected Location")
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> getCityName());
        return view;
    }

    private void getCityName() {
        if (selectedLocation == null) {
            Toast.makeText(requireContext(), "Please select a location first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        ProgressDialog progress = ProgressDialog.show(requireContext(),
                "Finding Location",
                "Looking up address...",
                true);

        // Reset timeout tracking
        isGeocodingComplete = false;
        geocodingHandler = new Handler();
        geocodingHandler.postDelayed(geocodingTimeoutRunnable, GEOCODING_TIMEOUT_MS);

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        selectedLocation.latitude,
                        selectedLocation.longitude,
                        1
                );

                isGeocodingComplete = true;
                geocodingHandler.removeCallbacks(geocodingTimeoutRunnable);

                requireActivity().runOnUiThread(() -> {
                    progress.dismiss();

                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String cityName = address.getLocality();
                        if (cityName == null) cityName = address.getAdminArea();
                        returnCityName(cityName != null ? cityName : "Unknown Location");
                    } else {
                        Toast.makeText(requireContext(),
                                "Location not found",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                isGeocodingComplete = true;
                geocodingHandler.removeCallbacks(geocodingTimeoutRunnable);

                requireActivity().runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(requireContext(),
                            "Error looking up address",
                            Toast.LENGTH_SHORT).show();
                    returnCityName(selectedLocation.latitude + ", " + selectedLocation.longitude);
                });
            }
        }).start();
    }

    private void returnCityName(String locationName) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("CITY_NAME", locationName);
        requireActivity().setResult(requireActivity().RESULT_OK, resultIntent);
        requireActivity().finish();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}