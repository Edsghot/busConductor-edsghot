package app.edsghot.busunamba;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import app.edsghot.busunamba.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class conductor extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    static final int REQUEST_CODE_LOCATION = 0;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private Marker busMarker;
    private DocumentReference locationRef;
    private DocumentReference mensajeDisponible;

    private boolean isServiceRunning = false;


    private int contView = 0;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Mapaconductor);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        locationRef = db.collection("ubicacion").document("gps");
        mensajeDisponible = db.collection("mensaje").document("49dsRj4pFVxO99qhcGHQ");


        Button backButton = findViewById(R.id.btnBack);
        ImageView rutaImage = findViewById(R.id.rutaImage);
        rutaImage.setVisibility(View.GONE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contView%2 == 0){
                    rutaImage.setVisibility(View.VISIBLE);
                    if (!isServiceRunning) {
                        // Iniciar el servicio solo si no está en ejecución
                        Intent serviceIntent = new Intent(conductor.this, LocationIntentService.class);
                        startService(serviceIntent);
                        isServiceRunning = true;
                    }

                    contView++;
                }else{
                    rutaImage.setVisibility(View.GONE);
                    if (isServiceRunning) {
                        // Detener el servicio solo si está en ejecución
                        Intent serviceIntent = new Intent(conductor.this, LocationIntentService.class);
                        stopService(serviceIntent);
                        isServiceRunning = false;
                    }
                    contView++;
                }
            }
        });


    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Switch disponibilidad = findViewById(R.id.disponibilidad);
        this.googleMap = googleMap;

            setupMapSettings();
            locationRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            double latitude = (double) data.get("latitude");
                            double longitude = (double) data.get("longitude");

                            LatLng locationLatLng = new LatLng(latitude, longitude);

                            //==========================================================
                            disponibilidad.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (disponibilidad.isChecked()) {
                                        Map<String, Object> datosMensaje = new HashMap<>();
                                        datosMensaje.put("mensaje", "El bus esta operativo");
                                        datosMensaje.put("check", true);
                                        // Agrega más campos y valores según sea necesario

                                        // Sube los datos del mensaje a la colección de mensajes
                                        mensajeDisponible.set(datosMensaje)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // La operación de subida fue exitosa
                                                        // Puedes realizar cualquier acción adicional después de subir los datos correctamente
                                                        Toast.makeText(conductor.this, "Disponible", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        disponibilidad.setChecked(false);
                                                    }
                                                });
                                    } else {
                                        Map<String, Object> datosMensaje = new HashMap<>();
                                        datosMensaje.put("mensaje", "no operativo");
                                        datosMensaje.put("check", false);
// Agrega más campos y valores según sea necesario
// Sube los datos del mensaje a la colección de mensajes
                                        mensajeDisponible.set(datosMensaje)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // La operación de subida fue exitosa
                                                        // Puedes realizar cualquier acción adicional después de subir los datos correctamente
                                                        Toast.makeText(conductor.this, "no disponible", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        disponibilidad.setChecked(true);
                                                    }
                                                });
                                    }
                                }
                            });
                            //================================================================

                            // Agregar marcador en la ubicación recuperada

                            // Mover la cámara al marcador
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 10f));

                        }
                    }
                }
            });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }


    }

    @SuppressLint("MissingPermission")
    private void setupMapSettings() {
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y activa los permisos", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Tienes que activar los permisos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng currentLocation = new LatLng(latitude, longitude);
        locationRef.set(currentLocation);

        if (busMarker == null) {
            busMarker = googleMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Bus 1")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus__2_)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
        } else {
            busMarker.setPosition(currentLocation);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}