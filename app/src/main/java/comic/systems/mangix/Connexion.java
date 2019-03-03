package comic.systems.mangix;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.customtabs.CustomTabsIntent;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connexion extends AppCompatActivity {

    public static String PREFS_NAME     = "mapref";
    public static String PREF_USER      = "user";
    public static String PREF_MAIL      = "mail";

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT  = 10000;
    public static final int READ_TIMEOUT        = 15000;

    static final String SITE_INSCRIPTION = "https://comic.systems/contact";

    // private static final int REQUEST_READ_CONTACTS = 0;

    // Define variables for custom tabs and its builder
    CustomTabsIntent customTabsIntent;
    CustomTabsIntent.Builder intentBuilder;

    // UI references.
    private EditText mUser;
    private EditText mPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        // Initialize intentBuilder
        intentBuilder = new CustomTabsIntent.Builder();

        // Set toolbar(tab) color of your chrome browser
        intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        // Define entry and exit animation
        intentBuilder.setStartAnimations(this, R.anim.left_to_right_start, R.anim.right_to_left_start);
        // intentBuilder.setStartAnimations(this, R.anim.push_up_in, R.anim.push_up_out);
        intentBuilder.setExitAnimations(this, R.anim.right_to_left_end, R.anim.left_to_right_end);
        // intentBuilder.setExitAnimations(this, R.anim.push_down_in, R.anim.push_down_out);
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        // build it by setting up all
        customTabsIntent = intentBuilder.build();

        // Set up the login form.
        mUser = (EditText) findViewById(R.id.connexion_mail);
        mPass = (EditText) findViewById(R.id.connexion_pass);
    }

    // Ouvrir la Chrome Custom Tab pour créer un compte
    public void Inscription(View arg0) {
        // go to website defined above
        customTabsIntent.launchUrl(this, Uri.parse(SITE_INSCRIPTION));

    }


    // Vérification au lancement
    public void onStart(){
        super.onStart();
        //read username and password from SharedPreferences
        getCompte();
    }

    // La vérification de base qui lance la validation des 2 champs
    public void checkLogin(View arg0){

        // Get text from user and pass field
        final String mail = mUser.getText().toString();
        final String pass = mPass.getText().toString();

        if (!isMailOk(mail)) {
            //Set error message for email field
            mUser.setError("Mail inccorect");
        }

        if (!isPassOk(pass)) {
            //Set error message for password field
            mPass.setError("Minimum 6 caractères");
        }

        if(isMailOk(mail) && isPassOk(pass))
        {
            new AsyncLogin().execute(mail, pass);
        }

    }

    // Vérifier si le nom d'utilisateur est bien rentré
    private boolean isUserOk(String champ) {
        if (champ != null && champ.length() >= 5) {
            return true;
        }
        return false;
    }

    // Vérifier si le mot de passe est bien rentré
    private boolean isPassOk(String champ) {
        if (champ != null && champ.length() >= 6) {
            return true;
        }
        return false;
    }

    // Vérifier si le mail est bien rentré
    private boolean isMailOk(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Classe -> Envoyer les champs de connexion
    private class AsyncLogin extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(Connexion.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tChargement...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }
        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("https://android.comic.systems/mangix/connexion");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("mail", params[0])
                        .appendQueryParameter("pass", params[1]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if (result.contains("false")){
                Toast.makeText(Connexion.this, "Aucun compte n'a été trouvé", Toast.LENGTH_LONG).show();
            } else if (result.contains("verif")){
                Toast.makeText(Connexion.this, "Mot de passe incorrect", Toast.LENGTH_LONG).show();
            } else if (result.contains("exception") || result.contains("unsuccessful")) {
                Toast.makeText(Connexion.this, "Oops! Il y a un problème de connexion...", Toast.LENGTH_LONG).show();
            }else{
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */
                Toast.makeText(Connexion.this, "Bienvenue "+result+" !", Toast.LENGTH_LONG).show();
                rememberMe(mUser.getText().toString(), result);
                getCompte();
            }
        }

    }

    // Garder en mémoire les paramètres
    public void rememberMe(String mail, String user){
        //save username and password in SharedPreferences
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                .edit()
                .putString(PREF_MAIL, mail)
                .putString(PREF_USER, user)
                .commit();
    }

    // Récupérer l'utilisateur
    public static String getUser(Context context) {
        SharedPreferences settings;
        String text;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREF_USER, null);
        return text;
    }

    // Récupérer l'adresse mail
    public static String getMail(Context context) {
        SharedPreferences settings;
        String text;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREF_MAIL, null);
        return text;
    }

    // Enregistrer l'utilisateur
    public void getCompte(){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        String prefUser = pref.getString(PREF_USER, null);
        String prefMail = pref.getString(PREF_MAIL, null);

        if (prefUser != null && prefMail != null) {
            //directly show logout form
            // autoConnect(prefUser, prefMail);
            autoConnect();
        }
    }

    // Se connecter avec les paremètres gardés en mémoire
    // public void autoConnect(String utilisateur){
    public void autoConnect(){
        //display log out activity
        Intent intent   = new Intent(this, Accueil.class);
        /*
        intent.putExtra("user", prefUser);
        intent.putExtra("typeCompte", prefType);
        */
        startActivity(intent);
    }
}

