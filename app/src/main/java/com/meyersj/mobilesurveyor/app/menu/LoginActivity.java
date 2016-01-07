package com.meyersj.mobilesurveyor.app.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Args;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;


public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";
    private final String SETLINE = "com.meyersj.mobilesurveyor.app.SETLINE";

    private Context context;
    private EditText username;
    private EditText password;
    private Button login, skip_login;
    private Properties prop;
    private HttpClient client;

    private static final int RESULT_SETTINGS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        loadPreferences(context);

        //TODO move to seperate class with all other POSTs
        client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();

        //10 second timeout
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);


        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        skip_login = (Button) findViewById(R.id.skip_login);

        prop = Utils.getProperties(getApplicationContext(), Cons.PROPERTIES);
        final String test_user = prop.getProperty(Cons.TEST_USER);
        final String anon_name = prop.getProperty(Cons.ANON_NAME);
        final String anon_pass = prop.getProperty(Cons.ANON_PASS);

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = username.getText().toString();
                String pass = password.getText().toString();


                String[] params = new String[3];
                params[0] = Utils.getUrlApi(context) + "/verifyUser";
                params[1] = name;
                params[2] = pass;

                //close keypad
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                if (getCurrentFocus() != null) {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }

                if(name.equals(anon_name) && pass.equals(anon_pass)) {
                    password.setText("");
                    startCollection(anon_name);
                }
                else {
                    //verify login credentials
                    //start SetLineActivity if credentials are valid
                    VerifyLoginTask task = new VerifyLoginTask();
                    task.execute(params);
                }
            }
        });

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.d(TAG,"Enter pressed");
                    login.performClick();
                }
                return false;
            }
        });

        skip_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startCollection(test_user);
            }
        });
    }

    protected void startCollection(String username) {
        Intent intent = new Intent(SETLINE);
        intent.putExtra(Cons.USER_ID, username);
        startActivity(intent);
    }

    class VerifyLoginTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            Log.d(TAG, "url:" + params[0]);
            Log.d(TAG, "username:" + params[1]);
            Log.d(TAG, "password:" + params[2]);
            return post(params);
        }
        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                validateResponse(response);
            }
        }
    }

    protected String post(String[] params) {
        String responseString = null;
        HttpPost post = new HttpPost(params[0]);

        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Args.Login.USER_NAME, params[1]));
        postParam.add(new BasicNameValuePair(Args.Login.PASSWORD, params[2]));

        try {
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);
            HttpEntity entityR = response.getEntity();
            responseString = EntityUtils.toString(entityR);
            Log.d(TAG, responseString);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException");
            Log.e(TAG, e.toString());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.toString());
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
            Log.e(TAG, e.toString());
        }
        return responseString;
    }

    private void validateResponse(String jsonInput) {

        JSONParser parser = new JSONParser();

        try{
            Object obj = parser.parse(jsonInput);
            JSONObject results = (JSONObject) obj;

            String match = results.get(Args.Login.MATCH).toString();
            String user_id = results.get(Args.Login.USER_ID).toString();

            if (match.equals("false")) {
                Log.d(TAG, "no matching username-password");
                Utils.shortToastCenter(context, "No record of that username-password.");
            }

            //user and password match
            //move user to SetLineActivity
            else {
                password.setText("");
                startCollection(user_id);
            }

        } catch(ParseException pe){
            Log.e(TAG, pe.toString());
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            //start settings activity
            case KeyEvent.KEYCODE_MENU:
                Intent i = new Intent(context, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
            default:
                return super.onKeyDown(keycode, e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(context, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void loadPreferences(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        //this should only execute after program was installed for first time
        //grab default urls from properties and update sharedprefs with those
        if(!sharedPref.contains(Cons.SET_PREFS)) {
            Properties prop = Utils.getProperties(context, Cons.PROPERTIES);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Cons.SET_PREFS, true);
            editor.putString(Cons.BASE_URL, prop.getProperty(Cons.BASE_URL));
            editor.putString(Cons.SOLR_URL, prop.getProperty(Cons.SOLR_URL));
            Log.d(TAG, prop.getProperty(Cons.MAP_RTES));
            editor.putString(Cons.MAP_RTES, prop.getProperty(Cons.MAP_RTES));
            editor.apply();
        }
    }
}
