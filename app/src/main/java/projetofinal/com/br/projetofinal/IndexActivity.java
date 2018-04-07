package projetofinal.com.br.projetofinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

public class IndexActivity extends AppCompatActivity {

    JSONObject response, profilePicData, profilePicUrl;
    static String TAG = IndexActivity.class.getName();

    TextView tvUserName;
    ImageView ivUserPicture;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String userName, userEmail, userPicture;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        /*
         * Instanciamos os componentes de tela do layout
         */

        tvUserName = findViewById(R.id.txtNome);
        ivUserPicture = findViewById(R.id.profilePic);

        getUserData();
    }

    private void getUserData() {

        /*
         * Inicialmente recuperamos os dados do usuário que foram enviados via Intent
         */

        try {


            /*
             * Recuperamos o objeto USER_DATA, caso ele exista o objeto sharedPreferences será setado
             */

            sharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

            /*
             * Se a sessão já tiver sido ininicida, usamos os objetos do sharedPreferences
             */

            if (sharedPreferences.contains("LOGIN_SESSION")) {

                userEmail = sharedPreferences.getString("email", "");
                userName = sharedPreferences.getString("name", "");
                userPicture = sharedPreferences.getString("picture", "");


                tvUserName.setText(userName);
                Picasso.with(this).load(userPicture).into(ivUserPicture);

            } else {

                Intent intent = getIntent();
                String jsondata = intent.getStringExtra("userProfile");

                Log.d(TAG, "JSON: " + jsondata);

                response = new JSONObject(jsondata);

                /*
                 * Recuperamos os respectivos campos retornados no JSON e os setamos nos componentes de tela
                 */


                try {
                    userEmail = response.get("email").toString();
                } catch (Exception e) {
                    //Do Nothing
                }

                try {
                    userName = response.get("name").toString();
                } catch (Exception e) {
                    userName = response.get("userName").toString();
                }


                /*
                 * Habilitamos o modo de edição do sharedPreferences
                 */

                editor = sharedPreferences.edit();
                try {
                    profilePicData = new JSONObject(response.get("picture").toString());
                    profilePicUrl = new JSONObject(profilePicData.getString("data"));
                    userPicture = profilePicUrl.getString("url");
                    editor.putString("picture", userPicture);
                } catch (Exception e) {
                    //DO Nothing
                }
                /*
                 * Adicionamos KEYs que representam os dados do usuário ao objeto USER_DATA
                 */

                editor.putString("name", userName);
                editor.putString("email", userEmail);
                editor.putBoolean("LOGIN_SESSION", true);

                /*
                 * Salvamos as keys criadas
                 */

                editor.commit();
            }

            /*
             * Setamos os dados dos usuário nos componentes de tela
             */

            tvUserName.setText(userName);
            Picasso.with(this).load(userPicture).into(ivUserPicture);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void logout(View view) {

        Log.d(TAG, "Finalizando sessão do usuário");

        /*
         * Método que encerra a sessão do usuário no Facebook
         */

        LoginManager.getInstance().logOut();

        /*
         * Removemos os dados do usuário que estão no sharedPreferences
         */

        //sharedPreferences.edit().remove("USER_DATA").commit();
        sharedPreferences.edit().clear().commit();

        Intent intent = new Intent(IndexActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_index, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_item1) {
            logout(this.getCurrentFocus());
            Toast.makeText(getApplicationContext(), "Fazendo Logout", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}