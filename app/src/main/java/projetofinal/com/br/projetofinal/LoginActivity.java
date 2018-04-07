package projetofinal.com.br.projetofinal;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.ProgressDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    static String TAG = LoginActivity.class.getName();
    private LoginButton loginButton;
    private TextView info;
    private CallbackManager callbackManager;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkUserSession();

        final Button btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String email =  ((EditText) findViewById(R.id.edtEmail)).getText().toString();
                String senha =  ((EditText) findViewById(R.id.edtSenha)).getText().toString();

                autentica(email, senha);
            }
        });




        info = findViewById(R.id.txtInfo);
        loginButton = findViewById(R.id.login_button);

        callbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions("email");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                getUserDetailsFb(loginResult);
                Log.d(TAG, "Login realizado com sucesso");
            }

            @Override
            public void onCancel() {

                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {

                info.setText("Login attempt failed.");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    public void createUser(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateUserActivity.class);
        startActivity(intent);
    }


    private void checkUserSession() {

        SharedPreferences sharedPreferences = getSharedPreferences("USER_DATA", MODE_PRIVATE);

        if(sharedPreferences.contains("LOGIN_SESSION")){

            Log.d(TAG, "O usário já está logado, direcionando ele para a UserProfileActivity");

            Intent intent = new Intent(LoginActivity.this, IndexActivity.class);
            startActivity(intent);
        }
    }

    protected void getUserDetailsFb(LoginResult loginResult) {

        GraphRequest data_request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject json_object,
                            GraphResponse response) {
                        Intent intent = new Intent(LoginActivity.this, IndexActivity.class);
                        intent.putExtra("userProfile", json_object.toString());
                        startActivity(intent);
                    }

                });

        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email,picture.width(240).height(240)");
        data_request.setParameters(permission_param);
        data_request.executeAsync();
    }
    protected void getUserDetails(JSONObject loginResult) {

        Intent intent = new Intent(LoginActivity.this, IndexActivity.class);
        intent.putExtra("userProfile", loginResult.toString());
        startActivity(intent);
    }


    public void autentica(String email, String senha){
        String url = "http://ec2-34-230-46-185.compute-1.amazonaws.com:8080/v1/users/authenticate";

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject postRequest = new JSONObject();

        try {
            postRequest.put("email", email);
            postRequest.put("pass", senha);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postRequest,
                new Response.Listener<JSONObject>() {

                    /* Callback chamado em caso de sucesso */

                    @Override
                    public void onResponse(JSONObject response) {

                        progressDialog.dismiss();

                        String sucess = response.optString("sucess");
                        String message = response.optString("message");
                        if(sucess.equals("true")){
                            showDialog("Informação", "Login realizado com sucesso!");
                            Intent intent = new Intent(LoginActivity.this, IndexActivity.class);
                            startActivity(intent);
                            getUserDetails(response);
                            showDialog("Informação", message);
                        }else{
                            String msg = response.optString("message");
                            showDialog("Informação", message);
                            TextView txtInfo = findViewById(R.id.txtInfo);
                            txtInfo.setText(msg);
                        }



                    }
                },

                /* Callback chamado em caso de erro */

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.e(TAG, "Ocorreu um erro ao chamar a API " + error);
                        progressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //add params <key,value>
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<String, String>();
                String auth = "Basic MzgxNTc5ZmEtZDI0MC00Mzg3LTkyNTMtZWY2YjgwYTdhMWEwOmM4NDM4M2Y0LTJiMDgtNGJiYy04MjQwLWI0YjQ5YTFlYWQzZQ==";
                headers.put("Authorization", auth);
                return headers;
            }
        };

        queue.add(jsonObjReq);
        showProgressDialog();
    }




    private void showProgressDialog() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Por favor, aguarde");
        progressDialog.show();
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}

