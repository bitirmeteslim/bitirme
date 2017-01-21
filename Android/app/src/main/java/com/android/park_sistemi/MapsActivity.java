package com.android.park_sistemi;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.park_sistemi.Modules.DirectionFinder;
import com.android.park_sistemi.Modules.DirectionFinderListener;
import com.android.park_sistemi.Modules.GetCurrentLocation;
import com.android.park_sistemi.Modules.Route;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,DirectionFinderListener {

    private GoogleMap mMap;
    private Button btnBul;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private BroadcastReceiver broadcastReceiver;
    private TextView tv;
    private ProgressDialog progressDialog;
    private EditText etHedef;
    private String DELETE_COLLS_URL ="http://bitirmeservis.herokuapp.com/delete_colls";

    public void setDestination(String destination) {this.destination = destination;}
    public void setOrigin(String origin) {this.origin = origin;}
    public void setEmail(String email) {this.email = email;}
    public void setmLatitude(String mLatitude) {this.mLatitude = mLatitude;}
    public void setmLongitude(String mLongitude) {this.mLongitude = mLongitude;}

    private String email;
    private String origin;
    private String destination;
    private String mLatitude;
    private String mLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        tv = (TextView) findViewById(R.id.txtbundle);
        Intent intentEXtra = getIntent();
        Bundle bundle = intentEXtra.getExtras();
        //Intent gövdesinden gerekli bilgilerin alınması için bundle nesnesinin boş olup olmadığı kontrol ediliyor.
        if (bundle != null) {
            //latitude değişkenine değer veriliyor.
            setmLatitude((String) bundle.get("latitude"));
            //longitude değişkenine değer veriliyor.
            setmLongitude((String) bundle.get("longitude"));
            //destination değişkenine değer veriliyor.
            setDestination((String) bundle.get("destination"));
            //origin değişkenine değer veriliyor.
            setOrigin(mLatitude+","+mLongitude);
            //email intent body'den alınıp değişkene değer veriliyor.
            setEmail((String) bundle.get("email"));
            tv.setText("Origin: "+mLatitude+","+mLongitude+"\nDestination: "+destination);
            //ardından sendRequest metodu ile istek atılıyor.
            sendrequest();
        }
    }

    private void sendrequest()
    {
        //konum bilgilerinin alınıp alınmadığı kontrol ediliyor.
        if(origin.isEmpty() || destination.isEmpty())
        {
            //konum bilgileri alınmadıysa kullanıcı bilgilendiriliyor.
            final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Uyarı");
            builder
                    .setMessage("Konum bilgileriniz alınamadı.\nLütfen tekrar deneyin")
                    .setCancelable(true)
                    .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //MainActivity'e yönlendiriliyor(yeniden seçim yapması için).
                            Intent intent = new Intent(MapsActivity.this,MainActivity.class);
                            startActivity(intent);
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else{
            try{
                //seçilen noktaya göre rota çizdirilene kadar kullanıcı bekletiliyor).
                progressDialog = ProgressDialog.show(MapsActivity.this,"Lütfen bekleyiniz","Rota Çizdiriliyor",true);
                //DirectionFinder class'ı çalıstırılıyor.
                new DirectionFinder(this,origin,destination).execute();
            }catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
    }

   /* @Override
    protected void onDestroy() {
        super.onDestroy();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_COLLS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("email",getEmail().toString());
                return params;
            }
        };
    }*/

    //MainActivity'de anlatılmıştı.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {

            }
            else{
                runtime_permissions();
            }
        }
    }

    //MainActivity'de anlatılmıştı.
    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                },100);
                return true;
            }
            return false;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            LatLng latLng = new LatLng(Double.parseDouble(mLatitude),Double.parseDouble(mLongitude));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(mLatitude),Double.parseDouble(mLongitude)),15));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(40),2000,null);
        }
    }

    //DirectionFinderListener Interface'indeki metodu override ediyoruz.
    @Override
    public void onDirectionFinderStart() {

        //originMarkers'ın boş olup olmadığı kontrol ediliyor.
        if(originMarkers != null)
        {
            //boş değilse tüm markerlar siliniyor.
            for(Marker marker : originMarkers)
            {
                marker.remove();
            }
        }

        //destinationMarkers'ın boş olup olmadığı kontrol ediliyor.
        if(destinationMarkers != null)
        {
            //boş değilse tüm markerlar siliniyor.
            for(Marker marker : destinationMarkers)
            {
                marker.remove();
            }
        }

        //polylinePaths'ın boş olup olmadığı kontrol ediliyor.
        if(polylinePaths != null)
        {
            //boş değilse polylinePaths'ler silinyior.
            for(Polyline polyline : polylinePaths)
            {
                polyline.remove();
            }
        }
    }

    //konum bilgileri başarılı bir şekilde bulunduktan sonra onDirectionFinderSuccess metodu yardımıyla harita
    // harita üzerinde rota çizdiriliyor.
    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        //pollinePath için yeni dizi tanımlanıyor.
        polylinePaths = new ArrayList<>();
        //originMarkers için yeni dizi tanımlanıyor.
        originMarkers = new ArrayList<>();
        //destinationMarkers için yeni dizi tanımlanıyor.
        destinationMarkers = new ArrayList<>();

        //Rotalar için originMarkers'lar, destinationMarkers'lar ve polylinePaths'ler dolduruluyor.
        for(Route route : routes)
        {
            //originMarkers dizisi dolduruluyor
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    //bulunduğunuz konum(başlangıç noktası) mavi baloncuk(A) icon'u ile gösteriliyor.
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    //title olarak başlangıç adresimiz belirleniyor.
                    .title(route.startAddress)
                    .position(route.startLocation)
            ));
            //destinationMarkers dizisi dolduruluyor
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    //bulunduğunuz konum(başlangıç noktası) yeşil baloncuk(B) icon'u ile gösteriliyor.
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    //title olarak varış/hedef adresimiz belirleniyor.
                    .title(route.endAddress)
                    .position(route.endLocation)
            ));

            //PolylineOptions ile çizdirilecek yolun özellikleri belirleniyor.
            PolylineOptions polylineOptions = new PolylineOptions()
                    //yol boyunca yeryüzü'nün gösterilmesine izin veriliyor.
                    .geodesic(true)
                    //yol rengi kırmızı olarak belirleniyor
                    .color(Color.RED)
                    //çizginin kalınlığı belirleniyor
                    .width(10);

            //for döngüsü ile başlangıçtan hedefe olan aralıkta tüm noktalar tek tek alınıyor.
            for(int i = 0; i < route.points.size(); i++)
            {
                //polylineOptions'a tüm noktalar ekleniyor.
                polylineOptions.add(route.points.get(i));
            }
            //başlangıçtan hedefe polylineOptions'a eklenen tüm noktalara göre rota çizdiriliyor.
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
        //progressDialog kapatılıyor.
        progressDialog.dismiss();
    }
}
