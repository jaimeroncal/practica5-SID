package es.unizar.eina.p5sid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final static String TAG = "MainActivity";
    private AsyncTaskMain myTask = null;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location localizacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Botón buscar
        ImageButton botonAnadir = (ImageButton) findViewById(R.id.search);
        botonAnadir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                llamarATarea();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void llamarATarea() {
        // Para obtener el valor devuelto en onRetainCustomNonConfigurationInstance
        myTask = (AsyncTaskMain) getLastCustomNonConfigurationInstance();
        if (myTask == null) {
            // Evita crear una AsyncTask cada vez que, por ejemplo, hay una rotación
            Log.i(TAG, "onCreate: About to create MyAsyncTask");

            // get location
            getLocation();

            myTask = new AsyncTaskMain(this);
            myTask.execute(localizacion);
        } else
            myTask.attach(this);
        Toast.makeText(this, "Buscando en Flickr", Toast.LENGTH_LONG).show();
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this,
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            localizacion.setLatitude(location.getLatitude());
                            localizacion.setLongitude(location.getLongitude());
                        }
                    }
                }
        );
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance()
    {
        // Además de devolver mi tarea, elimino la referencia en mActivity
        myTask.detach();
        // Devuelvo mi tarea, para que no se cree de nuevo cada vez
        return myTask;
    }

    public void setupAdapter(Integer integer)
    {
        if (integer != -1)
            Toast.makeText(MainActivity.this,
                    "Codigo de respuesta: " + integer, Toast.LENGTH_LONG).show();
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
