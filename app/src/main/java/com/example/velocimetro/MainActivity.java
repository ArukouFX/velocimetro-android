package com.example.velocimetro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import android.Manifest;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private LocationManager locationManager;
    private TextView speedTextView, distanceTextView, avgSpeedTextView, travelTimeTextView;
    private double totalDistance = 0;
    private Location lastLocation;
    private long startTime = 0;  // Tiempo de inicio del viaje
    private boolean firstLocation = true;  // Para verificar la primera ubicación

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedTextView = findViewById(R.id.speedTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        avgSpeedTextView = findViewById(R.id.avgSpeedTextView); // Para velocidad promedio
        travelTimeTextView = findViewById(R.id.travelTimeTextView); // Para tiempo de viaje

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Verificar y solicitar permisos de ubicación si es necesario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this); // Ajustado a 2 segundos
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Método para reiniciar el odómetro, el tiempo de viaje y la velocidad promedio
    public void resetOdometer(View view) {
        totalDistance = 0;
        lastLocation = null;  // Reiniciar lastLocation para evitar saltos de distancia
        startTime = System.currentTimeMillis(); // Reiniciar tiempo de viaje
        distanceTextView.setText(String.format(Locale.getDefault(), "Distancia: %.2f km", totalDistance));
        avgSpeedTextView.setText("Velocidad promedio: 0.00 km/h");
        travelTimeTextView.setText("Tiempo de viaje: 0 s");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si se concede el permiso, solicitar actualizaciones de ubicación
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this); // Ajustado a 2 segundos
                }
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Calcular velocidad en km/h
        float speed = location.getSpeed() * 3.6f;
        speedTextView.setText(String.format(Locale.getDefault(), "Velocidad: %.2f km/h", speed));

        // Calcular distancia recorrida
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location); // en metros
            totalDistance += distance / 1000; // Convertir a kilómetros
            distanceTextView.setText(String.format(Locale.getDefault(), "Distancia: %.2f km", totalDistance));

            // Calcular el tiempo de viaje
            long currentTime = System.currentTimeMillis();
            long elapsedTime = (currentTime - startTime) / 1000; // En segundos
            travelTimeTextView.setText(String.format(Locale.getDefault(), "Tiempo de viaje: %d s", elapsedTime));

            // Calcular la velocidad promedio
            if (elapsedTime > 0) {
                double avgSpeed = totalDistance / (elapsedTime / 3600.0); // en km/h
                avgSpeedTextView.setText(String.format(Locale.getDefault(), "Velocidad promedio: %.2f km/h", avgSpeed));
            }
        }

        // Inicializar el tiempo de inicio en la primera ubicación
        if (firstLocation) {
            startTime = System.currentTimeMillis();
            firstLocation = false;
        }
        lastLocation = location;
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "GPS habilitado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "GPS deshabilitado, por favor activarlo.", Toast.LENGTH_SHORT).show();
    }
}
