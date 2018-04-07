package projetofinal.com.br.projetofinal;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateUserActivity extends AppCompatActivity {

    static String TAG = CreateUserActivity.class.getName();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        final Button btnCadastro = findViewById(R.id.btnCadastrar);
        btnCadastro.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                String nome = ((EditText) findViewById(R.id.edtNome)).getText().toString();
                String email = ((EditText) findViewById(R.id.edtEmail)).getText().toString();
                String senha = ((EditText) findViewById(R.id.edtSenha)).getText().toString();
                createUser(nome, email, senha);
            }
        });


    }


    public void createUser(String nome, String email, String senha) {
        String url = "http://ec2-34-230-46-185.compute-1.amazonaws.com:8080/v1/users/";

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject postRequest = new JSONObject();

        try {
            postRequest.put("userName", nome);
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
                        Log.d(TAG, "API Response: " + response);
                        String message = response.optString("sucess");
                        if(message.equals("true")){
                            showDialog("Informação", "Usuário cadastrado com sucesso!");
                            Intent intent = new Intent(CreateUserActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }else{
                            String msg = response.optString("message");
                            showDialog("Erro ao criar usuario",msg);
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
        progressDialog = new ProgressDialog(CreateUserActivity.this);
        progressDialog.setMessage("Por favor, aguarde");
        progressDialog.show();
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(CreateUserActivity.this).create();
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
