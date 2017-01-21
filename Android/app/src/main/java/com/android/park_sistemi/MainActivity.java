package com.android.park_sistemi;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.park_sistemi.Modules.GetCurrentLocation;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public void setOrigin(String origin) {this.origin = origin;}

    public String getIntent_email() {
        return intent_email;
    }

    public void setIntent_email(String intent_email) {
        this.intent_email = intent_email;
    }
    public void setmLatitude(String mLatitude) {this.mLatitude = mLatitude;}

    public void setmLongitude(String mLongitude) {this.mLongitude = mLongitude;}

    private String intent_email;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private String origin;
    private BroadcastReceiver broadcastReceiver;
    private Button btnBul;
    private ProgressDialog progressDialog;
    //Service'e konum bilgilerinin gönderilip mesafelerin sıralanacağı endpoint
    private String GET_DİSTANCE_REQUEST_URL = "http://bitirmeservis.herokuapp.com/get_distance";
    //Service üzerinden sıralanan mesafelerin alınacağı endpoint
    private String EN_YAKIN_DORT_REQUEST_URL = "http://bitirmeservis.herokuapp.com/mesafe_sirala";
    //Eski kayıtların silinmesi için istek atılacak endpoint
    private String GECMİS_TEMİZLE_REQUEST_URL ="http://bitirmeservis.herokuapp.com/delete_colls";
    private TextView txtResponse;
    private String []AdresDizi = new String[5];
    private String []mesafeDizi = new String[5];
    private String []destinationDizi = new String[5];
    private String []listviewDizi = new String[5];
    private TextView txtSolUst;
    private TextView txtsagUst;
    private TextView txtsolAlt;
    private TextView txtsagAlt;
    private String mLatitude;
    private String mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Gelen intent nesnesi alınıyor.
        Intent intentEXtra = getIntent();
        Bundle bundle = intentEXtra.getExtras();
        //bundle sınıfı ile gelen intent nesnesinin dolu olup olmadığı kontrol ediliyor.
        if (bundle != null) {
            //dolu ise gövdeden email alınıyor.
            setIntent_email((String) bundle.get("email"));
        }
        //GridLayout üzerindeki textview'lerin nesneleri tanımlanıyor.
        txtSolUst = (TextView) findViewById(R.id.txtSolUst);
        txtsagUst = (TextView) findViewById(R.id.txtsagUst);
        txtsolAlt = (TextView) findViewById(R.id.txtsolAlt);
        txtsagAlt = (TextView) findViewById(R.id.txtsagAlt);
        //Runtime permissions'ların alınıp alınmadığı kontrol ediliyor.
        if(!runtime_permissions()){
            //Konum bilgileri alınana kadar kullanıcı bekletiliyor.
            progressDialog = ProgressDialog.show(MainActivity.this,"Lütfen bekleyiniz","Konum bilgileriniz alınıyor",true);
            //gecmisTemizle metodu kullanıcının daha önce database'e kaydettiği konum bilgileri, mesafe kayıtları için kayıtlı bilgiler siliniyor.
            gecmisTemizle();
            //Konum bilgileri alınıyor.
            Intent intent = new Intent(MainActivity.this, GetCurrentLocation.class);
            //BroadcastReceiver ile service kullanılarak intent başlatılıyor.
            startService(intent);
        }
        //noktaHesapla();
        //Sol üstteki textview'in tıklanıldığında tetiklenen metod.
        txtSolUst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Textview tıklandıktan sonra gerekli bilgiler intent ile MapsActivity'e gönderiliyor.
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("latitude",mLatitude);
                intent.putExtra("longitude",mLongitude);
                intent.putExtra("destination",destinationDizi[0]);
                intent.putExtra("email",getIntent_email().toString());
                startActivity(intent);
            }
        });
        //Sağ üstteki textview'in tıklanıldığında tetiklenen metod.
        txtsagUst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Textview tıklandıktan sonra gerekli bilgiler intent ile MapsActivity'e gönderiliyor.
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("latitude",mLatitude);
                intent.putExtra("longitude",mLongitude);
                intent.putExtra("destination",destinationDizi[1]);
                intent.putExtra("email",getIntent_email().toString());
                startActivity(intent);
            }
        });
        //Sol alttaki textview'in tıklanıldığında tetiklenen metod.
        txtsolAlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Textview tıklandıktan sonra gerekli bilgiler intent ile MapsActivity'e gönderiliyor.
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("latitude",mLatitude);
                intent.putExtra("longitude",mLongitude);
                intent.putExtra("destination",destinationDizi[2]);
                intent.putExtra("email",getIntent_email().toString());
                startActivity(intent);
            }
        });
        //Sağ alttaki textview'in tıklanıldığında tetiklenen metod.
        txtsagAlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Textview tıklandıktan sonra gerekli bilgiler intent ile MapsActivity'e gönderiliyor.
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("latitude",mLatitude);
                intent.putExtra("longitude",mLongitude);
                intent.putExtra("destination",destinationDizi[3]);
                intent.putExtra("email",getIntent_email().toString());
                startActivity(intent);
            }
        });
    }

    //Kullanıcının daha önce giris yaptığında hesaplanılan mesafe bilgileri siliniyor.
    private void gecmisTemizle(){

        //Requestqueue oluşturuluyor.
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        //StringRequest oluşturuluyor.
        //Burada Request Body'e eklenen email adresine ilişkin database'deki tüm kayıtlar siliniyor.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, GECMİS_TEMİZLE_REQUEST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Request başarılı bir şekilde geldikten sonra noktaHesapla metodu çağırılıyor.
                        noktaHesapla();
                    }
                }, new Response.ErrorListener() {
            @Override
            //Response ile ilgili hata alınırsa kullanıcı bilgilendiriliyor.
            public void onErrorResponse(VolleyError error) {
                //AlertDialog ile bilgilendirme yapılıyor.
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Uyarı");
                builder
                        .setMessage("Bir hata oluştu lütfen tekrar giriş yapın")
                        .setCancelable(true)
                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                //progressDialog kapatılıyor.
                                progressDialog.dismiss();
                                //LoginActivity'e yönlendirilip kullanıcının yeniden giriş yapılması isteniyor.
                                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }){
            //Request Body'e email ekleniyor.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("email",intent_email);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    //GetCurrentLocation service'i başlatıldıkton sonra onResume metodu ile broadcastReceiver dinleniyor.
   @Override
    protected void onResume() {
        super.onResume();
       //broadcastReceiver'in dolu olup olmadığı kontrol ediliyor.
       //dolu ise konum bilgileri geldiğinden gerekli yerler alınıyor.
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Double latitude = ((Double) intent.getExtras().get("latitude"));
                    Double longitude = ((Double) intent.getExtras().get("longitude"));
                    //setmLatitude metodu ile global'de tanımlı latitude değişkenine değer veriliyor.
                    setmLatitude(latitude.toString());
                    //setmLongitude metodu ile global'de tanımlı longitude değişkenine değer veriliyor.
                    setmLongitude(longitude.toString());
                    //Globaldeki origin nesnesine konum bilgilerimiz latitude+longitude aralarında virgül olacak
                    //şekilde gönderiliyor. bunun sebebi konum bilgilerinin google'a bu formatta gönderiliyor olması
                    setOrigin(latitude.toString() + "," + longitude.toString());
                    //konum bilgileri alındıktan sonra broadcastReceiver durdurularak gps'in çalısması durduruluyor.
                    unregisterReceiver(broadcastReceiver);
                    //service durduruluyor
                    stopService(intent);
                    //noktahesapla metodu çağrılarak konum bilgilerimiz ile service'e mesafelerin hesaplanması için
                    //istek atıyoruz
                    noktaHesapla();
                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    //Runtime sırasında herhangi bir permission'a gerek duyulduğunda otomatik olarak tetiklenip gerekli
    // izinlerin sağlanması için kullanıcıdan gerekli işlemler için izin isteniyor.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //kendi yazdığımız statü kodu var mı yokmu karşılaştırma yapılıyor.
        if(requestCode == 100)
        {
            //grantResultst dizisinin 0 ve 1. indisleri PackageManager.PERMISSION_GRANTED'e eşitse
            //yani gerekli izinler sağlanmışsa devam ediyor.
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                //enable_buttons();
            }
            else{
                runtime_permissions();
            }
        }
    }

    //Api level'in 23 ten büyük olup olmadığı kontrol ediliyor.
    private boolean runtime_permissions() {
        //Api level 23'ten büyükse ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, INTERNET permissions'ları request
        //permissions dizisine ekleniyor. request code onRequestPermissionsResult metodunda kullanılabilmesi için
        //elle 100 olarak belirleniyor. (gerekli izinler tanımlı değilse bu işlemler yapılıyor)
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

    //Konum bilgilerimiz service'e email adresimizle birlikte gönderiliyor.
    private void noktaHesapla(){
        //RequestQueue oluşturuluyor
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        //StringRequest oluşturuluyor
        StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_DİSTANCE_REQUEST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Konumumuzla service'teki lokasyonlar arasındaki mesafeler hesaplandıktan sonra en yakın
                        // olan 4 kayıt getiriliyor.
                        enYakinDortGetir();
                    }
                }, new Response.ErrorListener() {
            @Override
            //Response ile ilgili hata oluşunca tetiklenecek metod
            public void onErrorResponse(VolleyError error) {
            }
        }){
            //Request Body'e konum bilgilerimiz ve email adresi yükleniyo
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("origin",origin);
                params.put("email",getIntent_email().toString());
                return params;
            }
        };
        //Kuyruğa stringRequest ekleniyor.
        requestQueue.add(stringRequest);
    }

    //Hesaplanan konum bilgileri ile en yakın 4 adres getiriliyor.
    private void enYakinDortGetir(){
        //RequestQueue oluşturuluyor.
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        //String request oluşturuluyor.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EN_YAKIN_DORT_REQUEST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listViewDoldur(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        //StringRequest kuyruğa ekleniyor.
        requestQueue.add(stringRequest);
    }

    //En yakın 4 adres geldikten sonra textview'lere dolduruluyor.
    private void listViewDoldur(String response){
        progressDialog.dismiss();
        //Service'den gelen response dizi şeklinde olmadığı için response'u dizi gibi gösteriyoruz
        //JsonParse yapabilmemiz için dizi olması gerektiği için dizi haline getirmemiz gerekli
        String jsonDizi = "{mesafeler:"+response+"}";
        try {
            //Gelen jsondizisi parse ediliyor
            JSONObject jsonData = new JSONObject(jsonDizi);
            //mesafeler dizisi alınıyor
            JSONArray jsonArray = jsonData.getJSONArray("mesafeler");
            //dizideki jsonobject sayısı kadar döngü döndürülüyor
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //jsonObject'ten destination'un value'si alınıyor
                destinationDizi[i] = jsonObject.getString("destination");
                //jsonObject'ten Adres'in value'si alınıyor
                AdresDizi[i] = jsonObject.getString("Adres");
                //jsonObject'ten mesafe'nin value'si alınıyor
                mesafeDizi[i] = jsonObject.getString("mesafe");
                //Listview'e dolduruluyor
                listviewDizi[i] = "Adres: "+AdresDizi[i]+ "\nMesafe: "+mesafeDizi[i]+" km";
            }
            //Textview'ler listview'e doldurulan veriler ile dolduruluyor
            txtSolUst.setText(listviewDizi[0]);
            txtsagUst.setText(listviewDizi[1]);
            txtsolAlt.setText(listviewDizi[2]);
            txtsagAlt.setText(listviewDizi[3]);
        }
        //parse edilirken herhangi bir hata oluşursa exception fırlatılıyor.
        catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
