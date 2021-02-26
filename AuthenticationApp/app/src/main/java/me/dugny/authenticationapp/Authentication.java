package me.dugny.authenticationapp;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Authentication extends AppCompatActivity {
    // Layout elements references
    private EditText _loginInput;
    private EditText _passwordInput;
    private Button _authenticationButton;
    private TextView _resultTextView;

    /**
     * Authenticate using httpbin service.
     * Should NOT be called in the main thread.
     *
     * @param login    input login
     * @param password input password
     * @return a String containing the returned page from httpbin
     */
    private static String authenticateHTTP(String login, String password) {
        try {
            // Create the URL
            URL url = new URL("https://httpbin.org/basic-auth/bob/sympa");
            // Add the HTTP auth
            String authorization = String.format("Basic %s", Base64.encodeToString((login + ":" + password).getBytes(), Base64.NO_WRAP));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", authorization);

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // Parse and return
                return readStream(in);
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.e("AuthApp", String.format("IO error: %s", e.getMessage()));
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Parse an InputStream and return a String
     * https://stackoverflow.com/a/17167640/6010432
     *
     * @param is input stream
     * @return output String
     */
    private static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set activity
        setContentView(R.layout.activity_authentication);

        // Get references to UI
        this._loginInput = findViewById(R.id.loginInput);
        this._passwordInput = findViewById(R.id.passwordInput);
        this._authenticationButton = findViewById(R.id.authenticationBtn);
        this._resultTextView = findViewById(R.id.result);

        // Setup click handler
        this._authenticationButton.setOnClickListener(this::onClickAuthenticate);
    }

    /**
     * Method called on click of the authenticate button.
     *
     * @param v View to listen from (button view)
     */
    private void onClickAuthenticate(View v) {
        Log.i("AuthApp", "Button clicked");

        // Start the network request in a new separated thread
        Thread thread = new Thread(() -> {
            Log.i("AuthApp", "New thread!");

            String textToDisplay = "";

            // Parse the output
            try {
                // Get the result page from HttpBin, with our authentication
                String result = authenticateHTTP(_loginInput.getText().toString(), _passwordInput.getText().toString());
                // Parse it
                JSONObject jsonObject = new JSONObject(result);
                // Set the result
                textToDisplay = String.format("%s %s", getString(R.string.auth_status), jsonObject.get("authenticated"));
            } catch (JSONException e) {
                textToDisplay = getString(R.string.auth_status_error);
                Log.e("AuthApp", String.format("Parsing error: %s", e.getMessage()));
                e.printStackTrace();
            }

            // Update view
            String finalTextToDisplay = textToDisplay;
            runOnUiThread(() -> _resultTextView.setText(finalTextToDisplay));
        });
        thread.start();
    }
}