package app.edsghot.busunamba;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationIntentService extends IntentService implements OnMapReadyCallback, LocationListener {
    private static final int NOTIFICATION_ID = 123;
    private FusedLocationProviderClient fusedLocationClient;
    private DocumentReference locationRef;

    public LocationIntentService() {
        super("LocationIntentService");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        locationRef = db.collection("ubicacion").document("gps");

        // Configurar la notificación para el Foreground Service
        String channelId = "ubicacion_channel";
        String channelName = "Servicio de Ubicación";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Servicio de Ubicación")
                .setContentText("Servicio de ubicación en ejecución")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // No es necesario realizar operaciones adicionales aquí ya que las actualizaciones de ubicación se manejan en el método onLocationChanged.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Detener las actualizaciones de ubicación cuando el servicio se detiene
        fusedLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.d("LocationIntentService", "Ubicación actualizada: " + latitude + ", " + longitude);

        LatLng currentLocation = new LatLng(latitude, longitude);
        locationRef.set(currentLocation)
                .addOnSuccessListener(aVoid -> Log.d("LocationIntentService", "Ubicación subida con éxito"))
                .addOnFailureListener(e -> Log.e("LocationIntentService", "Error al subir la ubicación", e));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Implementa aquí la lógica que deseas realizar cuando el mapa está listo
    }
}
