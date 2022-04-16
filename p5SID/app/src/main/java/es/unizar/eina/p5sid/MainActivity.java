package es.unizar.eina.p5sid;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final static String TAG = "MainActivity";
    private AsyncTaskMain myTask = null;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location localizacion;
    private Marker markerMyPosition;
    private Marker markerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create location client and first location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // while no permission ask for it
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this,
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            localizacion = new Location("");
                            localizacion.setLatitude(location.getLatitude());
                            localizacion.setLongitude(location.getLongitude());
                        }
                    }
                }
        );
        // Botón buscar
        ImageButton botonAnadir = (ImageButton) findViewById(R.id.search);
        botonAnadir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                updateLocation();
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
            myTask = new AsyncTaskMain(this);
            myTask.execute(localizacion);
        } else
            myTask.attach(this);
        Toast.makeText(this, "Buscando en Flickr", Toast.LENGTH_LONG).show();
    }

    public void updateLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
        locationRequest.setInterval(0);
        // while no permission ask for it
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult result) {
                        if (result == null)
                            return;
                        int lastIndex = result.getLocations().size() - 1;
                        Location location = result.getLocations().get(lastIndex);
                        localizacion = new Location("");
                        localizacion.setLatitude(location.getLatitude());
                        localizacion.setLongitude(location.getLongitude());
                    }
                },
                Looper.getMainLooper()
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

    public void setupAdapter(AsyncTaskMain.Imagen imagen)
    {
        // INVISIBLE MESSAGE
        TextView message = (TextView) findViewById(R.id.empty);
        message.setVisibility(View.GONE);
        // SET TITLE
        TextView mTitulo = (TextView) findViewById(R.id.titulo);
        mTitulo.setText(imagen.titulo);
        mTitulo.setVisibility(View.VISIBLE);
        // SET LAT LONG
        TextView mLatLong = (TextView) findViewById(R.id.latlong);
        mLatLong.setText("Latitud: "+imagen.latitud+", Longitud: "+imagen.longitud);
        mLatLong.setVisibility(View.VISIBLE);
        // SET IMAGE
        ImageView mImagen = (ImageView) findViewById(R.id.imagen);
        mImagen.setImageBitmap(imagen.imagen);
        mImagen.setVisibility(View.VISIBLE);

        // Add a in my location and move the camera
        LatLng myPosition = new LatLng(localizacion.getLatitude(), localizacion.getLongitude());
        if (markerMyPosition!=null) markerMyPosition.remove();
        markerMyPosition = mMap.addMarker(new MarkerOptions()
                .position(myPosition)
                .title("Estás aquí"));
        markerMyPosition.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 13.0f));

        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(imagen.imagen, 60, 60,false));
        // Add a in my location and move the camera
        LatLng imagePosition = new LatLng(Double.parseDouble(imagen.latitud), Double.parseDouble(imagen.longitud));
        if (markerImage!=null) markerImage.remove();
        markerImage = mMap.addMarker(new MarkerOptions()
                .position(imagePosition)
                .title(imagen.titulo));
        markerImage.setIcon(bitmap);
    }
}
