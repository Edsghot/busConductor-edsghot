package app.edsghot.busunamba;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import app.edsghot.busunamba.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;

public class alumno extends AppCompatActivity implements OnMapReadyCallback{
    private GoogleMap googleMap;
    private DocumentReference locationRef;
    private ListenerRegistration locationListener;
    private DocumentReference mensajeDisponible;
    private TextView tvAviso;
    private int contMov = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumno_mapa);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        locationRef = db.collection("ubicacion").document("gps");
        mensajeDisponible = db.collection("mensaje").document("49dsRj4pFVxO99qhcGHQ");

        tvAviso = findViewById(R.id.tvAviso);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.MapaTurismo);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        setupMapSettings();
        locationListener = locationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        double latitude = (double) data.get("latitude");
                        double longitude = (double) data.get("longitude");

                        LatLng locationLatLng = new LatLng(latitude, longitude);

                        //recuperando mensaje?===============================================
                        //falta corregir
                        mensajeDisponible.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    Map<String, Object> data = documentSnapshot.getData();
                                    if (data != null) {
                                        Boolean check = (Boolean) data.get("check");
                                        String mensaje = (String) data.get("mensaje");

                                        if(check == true){
                                            tvAviso.setText(mensaje);
                                        }else{
                                            tvAviso.setText(mensaje);
                                        }
                                    }
                                }
                            }
                        });

                        // Agregar marcador en la ubicación recuperada
                        googleMap.clear(); // Limpiar marcadores anteriores
                        googleMap.addMarker(new MarkerOptions()
                                .position(locationLatLng)
                                .title("Ubicación Actualizada")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus__2_)));

                        // Mover la cámara al marcador
                        if(contMov == 0){
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 40f));
                            contMov ++;
                        }                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void setupMapSettings() {
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationListener != null) {
            locationListener.remove();
        }
    }
}