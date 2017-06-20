package br.com.jose.projectgooglemaps;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import br.com.jose.projectgooglemaps.domain.Router;
import br.com.jose.projectgooglemaps.listener.RoutingListener;
import br.com.jose.projectgooglemaps.service.DirectionServiceRouter;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener, View.OnClickListener {

    private GoogleMap mMap;
    private EditText mEditOrigin;
    private EditText mEditDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String mode = "walking";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_walking:
                    mode = "walking";
                    return true;
                case R.id.navigation_car:
                    mode = "driving";
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.fabMap);
        actionButton.setOnClickListener(this);

        initViews();
    }

    private void initViews() {
        mEditOrigin = (EditText) findViewById(R.id.editOrigin);
        mEditDestination = (EditText) findViewById(R.id.editDestination);
    }

    private void sendRequest() {
        String origin = mEditOrigin.getText().toString();
        String destination = mEditDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Campo partida obrigatório", Toast.LENGTH_LONG).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Campo destino obrigatório", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            new DirectionServiceRouter(this, origin, destination, this.mode).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        ////LatLng hcmus = new LatLng(10.762963, 106.682394);
        ////mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 18));
        ////originMarkers.add(mMap.addMarker(new MarkerOptions()
        ////        .title("José")
        ////       .position(hcmus)));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onDirectionStart() {
        progressDialog = ProgressDialog.show(this, "Carregando...",
                "Encontrando as rotas", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionSuccess(List<Router> routers) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        Log.i("onDirectionSuccess", routers.size() + "");

        if (routers.size() > 0) {
            for (Router route : routers) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.getStartLocation(), 16));
                Log.i("onDirectionSuccess", route.getStartLocation() + "");

                final AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setTitle("Informações");
                dialog.setMessage("Duração: " + route.getDuration().getText() + "\nDistância: " + route.getDistance().getText());
                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                        .title(route.getStartAddress())
                        .position(route.getStartLocation())));
                destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                        .title(route.getEndAddress())
                        .position(route.getEndLocation())));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);


                for (int i = 0; i < route.getPoits().size(); i++) {
                    polylineOptions.add(route.getPoits().get(i));

                    //Marker melbourne = mMap.addMarker(new MarkerOptions()
                    //        .position(route.getPoits().get(i))
                    //       .title(route.getStartAddress()));
                    //melbourne.showInfoWindow();
                }
                polylinePaths.add(mMap.addPolyline(polylineOptions));
            }
        } else {
            Toast.makeText(this, "Endereço incorreto, tente novamente!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        sendRequest();
    }
}
