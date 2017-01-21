package com.android.park_sistemi;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Button btnRegister;
    private TextView etPassword;
    private TextView etPasswordAgain;
    private TextView etEmail;
    //Service üzerinden kayıt yapılabilmesi için gerekli endpoint.
    private String REGİSTER_REQUEST_URL = "http://bitirmeservis.herokuapp.com/register";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        etEmail = (TextView) findViewById(R.id.etEmail);
        etPassword = (TextView) findViewById(R.id.etPassword);
        etPasswordAgain = (TextView) findViewById(R.id.etPasswordAgain);

        //Kaydol butonuna tıklanınca çalışacak olan event.
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //İşlemler yapılana kadar kullanıcı bekletiliyor.
                progressDialog = ProgressDialog.show(RegisterActivity.this,"","Lütfen bekleyiniz",true);
                //Textview'lerin dolu olup olmadığı kontrol ediliyor.
                if(TextUtils.isEmpty(etEmail.getText().toString())|| TextUtils.isEmpty(etPassword.getText().toString()) || TextUtils.isEmpty(etPasswordAgain.getText().toString())){
                    progressDialog.dismiss();
                    //Textview'lerden herhangi biri boş geçilirse kullanıcı alertDialog ile bilgilendiriliyor.
                    final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("Uyarı");
                    builder
                            .setMessage("Lütfen tüm alanları doldurun")
                            .setCancelable(true)
                            .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else{
                    //tüm alanlar doldurulduktan sonra girilen şifrelerin aynı olup olmadığı karşılaştırılıyor.
                    if(etPassword.getText().toString().matches(etPasswordAgain.getText().toString())){
                        //StringRequest oluşturuluyor.
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGİSTER_REQUEST_URL,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        progressDialog.dismiss();
                                        //Kayıt işlemi başarılı olduktan sonra kullanıcı bilgilendiriliyor.
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

                                        builder.setTitle("Uyarı");

                                        builder
                                                .setMessage("Başarıyla kayıt oldunuz")
                                                .setCancelable(true)
                                                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                        //AlertDialog "Tamam" butonuna tıklandıktan sonra LoginActivity'e yönlendiriliyor.
                                                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                                        startActivity(intent);
                                                    }
                                                });
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                    }
                                },
                                //Hata oluşması durumunda tetiklenecek yer
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        NetworkResponse networkResponse = error.networkResponse;
                                        //Response'un statü kodu 406 ise email'in sistemde kayıtlı olduğuna dair kullanıcı bilgilendiriliyor.
                                        if(networkResponse != null && networkResponse.statusCode == 406){
                                            progressDialog.dismiss();
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

                                            builder.setTitle("Uyarı");

                                            builder
                                                    .setMessage("Zaten kayıtlısınız")
                                                    .setCancelable(true)
                                                    .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                            Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                                            startActivity(intent);
                                                        }
                                                    });
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();
                                        }
                                        //Response'un statü kodu 500 ise service kısmında hata olduğundan kullanıcı bilgilendirilip tekrar
                                        //denemesi isteniyor.
                                        else if(networkResponse != null && networkResponse.statusCode == 500){
                                            progressDialog.dismiss();
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

                                            builder.setTitle("Uyarı");
                                            builder
                                                    .setMessage("Bilinmeyen bir hata oluştu lütfen tekrar deneyin")
                                                    .setCancelable(true)
                                                    .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            AlertDialog alertDialog = builder.create();
                                            alertDialog.show();
                                        }
                                    }
                                }){
                            //Email ve şifre Request Body'e ekleniyor.
                            @Override
                            protected Map getParams(){
                                Map<String,String> params = new HashMap<String, String>();
                                params.put("email",etEmail.getText().toString());
                                params.put("password",etPassword.getText().toString());
                                return params;
                            }

                        };
                        //RequestQueue oluşturuluyor.
                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                        //Network Thread kuyruğuna stringRequest ekleniyor.
                        requestQueue.add(stringRequest);
                    }
                    else{
                        //Girilen şifrelerin uyuşmadığına göre kullanıcı bilgilendiriliyor.
                        progressDialog.dismiss();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);

                        builder.setTitle("Uyarı");
                        builder
                                .setMessage("Girilen şifreler uyuşmuyor")
                                .setCancelable(true)
                                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        //"tamam" butonuna tıklandıktan sonra textview'ler boşaltılıp kullanıcının
                                        //şifreleri yeniden girmesi isteniyor
                                        etPassword.setText("");
                                        etPasswordAgain.setText("");
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }

            }
        });
    }
}
