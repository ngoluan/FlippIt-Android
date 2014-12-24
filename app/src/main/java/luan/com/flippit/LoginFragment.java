package luan.com.flippit;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    View mView = null;
    String email = null;
    String password = null;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final EditText emailTxt;
        final EditText passwordTxt;

        mView = inflater.inflate(R.layout.fragment_login, container,
                false);

        emailTxt = (EditText) mView.findViewById(R.id.email);
        passwordTxt = (EditText) mView.findViewById(R.id.password);

        final Button register = (Button) mView.findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                email = emailTxt.getText().toString();
                password = passwordTxt.getText().toString();
                sendServer("register");
            }
        });
        final Button login = (Button) mView.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                email = emailTxt.getText().toString();
                password = passwordTxt.getText().toString();

                sendServer("login");
            }
        });
        MyActivity.mActionBar.hide();

        return mView;
    }

    private void sendServer(final String type) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                // TODO Auto-generated method stub
                String result = postData();
                return result;
            }

            public String postData() {
                String line = "";
                BufferedReader in = null;

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = null;
                if (type.equals("login")) {
                    httppost = new HttpPost(GeneralUtilities.SERVER_PATH + "server/login_v2.php");
                } else {
                    httppost = new HttpPost(GeneralUtilities.SERVER_PATH + "server/reg_v2.php");
                }

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("password", password));
                    nameValuePairs.add(new BasicNameValuePair("targetID", MyActivity.regid));
                    nameValuePairs.add(new BasicNameValuePair("type", "android"));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpclient.execute(httppost);

                    in = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent()));

                    line = in.readLine();

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
                return line;
            }

            @Override
            protected void onPostExecute(String msg) {
                try {
                    JSONObject result = new JSONObject(msg);
                    Toast.makeText(MyActivity.mContext, result.getString("message"), Toast.LENGTH_LONG).show();
                    String error = result.optString("error");
                    if (error.isEmpty()) {
                        storeEmail(email);

                        MyActivity.mFragmentManager.beginTransaction()
                                .replace(R.id.container, new WelcomeFragment())
                                .commit();
                    } else {
                        Toast.makeText(MyActivity.mContext, result.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void storeEmail(String email) {
        SharedPreferences.Editor editor = MyActivity.mPrefs.edit();
        editor.putString("email", email);
        editor.commit();
    }

}
