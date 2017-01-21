package com.android.park_sistemi;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private TextView txtCreateAccount;
    private Button btnLogin;
    private String LOGİN_REQUEST_URL ="http://bitirmeservis.herokuapp.com/login";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        txtCreateAccount = (TextView) findViewById(R.id.txtCreateAccount);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        //Hesap oluştur isimli textview tıklandığında RegisterActivity'e yönlendirme yapılıyor.
        txtCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        //Giriş Yap Butonu'nun click event'i
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Bilgiler karşılaştırılana kadar progressDialog ile kullanıcının bekletileceği konusundan bilgilendirme yapılıyor.
                progressDialog = ProgressDialog.show(LoginActivity.this,"","Lütfen Bekleyiniz",true);
                //Textview'lerin boş geçilmemesi kontrolü yapılıyor.
                if(TextUtils.isEmpty(etEmail.getText().toString()) || TextUtils.isEmpty(etPassword.getText().toString())){
                    progressDialog.dismiss();
                    //Textview'ler boş ise alertDialog ile kullanıcı bilgilendiriliyor.
                    final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
                //Textview'ler dolu ise...
                else{
                    //StringRequest oluşturuluyor.
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGİN_REQUEST_URL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    //İstek başarılı bir şekilde gittikten sonra progressDialog kapatılıyor.
                                    progressDialog.dismiss();
                                    //LoginActivity'e geçiş yapılıyor.
                                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                    //Intent'in gövdesine email eklenerek başlatılıyor.
                                    intent.putExtra("email",etEmail.getText().toString());
                                    //Activity başlatılıyor
                                    startActivity(intent);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        //Gelen response'da herhangi bir hata varsa...
                        public void onErrorResponse(VolleyError error) {
                            NetworkResponse networkResponse = error.networkResponse;
                            //Response'un statü kodu 500 ise service ile ilgili bir sorun olduğundan tekrar denenmesi isteniyor
                            if(networkResponse != null && networkResponse.statusCode == 500){
                                progressDialog.dismiss();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
                            //Gelen statü kodu 404 ise kullanıcının kaydı bulunmamaktadır(E-mail database'de kayıtlı değil).
                            else if(networkResponse != null && networkResponse.statusCode == 404){
                                progressDialog.dismiss();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                                builder.setTitle("Uyarı");
                                builder
                                        .setMessage("Kayıt bulunamadı")
                                        .setCancelable(true)
                                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                etEmail.setText("");
                                                etPassword.setText("");
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                            //Gelen statü kodu 401 ise kullanıcının girdiği şifrenin yanlış olduğuna dair bilgilendirme yapılıyor.
                            else if(networkResponse != null && networkResponse.statusCode == 401){
                                progressDialog.dismiss();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle("Uyarı");
                                builder
                                        .setMessage("Girilen şifre yanlış")
                                        .setCancelable(true)
                                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                etPassword.setText("");
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        }
                    }){
                        //StringRequest ile gönderilecek parametreler Request Body'e yükleniyor.
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            //Girilen email ekleniyor
                            params.put("email",etEmail.getText().toString());
                            //Girilen şifre ekleniyor.
                            params.put("password",etPassword.getText().toString());
                            //Request Body'e eklenen parametreler return ediliyor.
                            return params;
                        }
                    };
                    //RequestQueue oluşturuluyor.
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    //Oluşturulan kuyruğa kendi oluşturduğumuz StringRequest objesi ekleniyor.
                    requestQueue.add(stringRequest);
                }
            }
        });
    }
}
