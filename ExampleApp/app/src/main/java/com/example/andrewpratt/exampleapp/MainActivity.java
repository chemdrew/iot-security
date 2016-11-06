package com.example.andrewpratt.exampleapp;

import android.content.Intent;
import android.nfc.NdefRecord;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.os.Bundle;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements CreateNdefMessageCallback {
    private static String TAG = "MainActivity";

    private class ViewHolder {
        private TextView receivedData;
        private EditText serverIp;
        private Button getPlainButton;
        private Button getTokenButton;
        private Button getDataButton;
        private Button sendPlainButton;
        private Button sendTokenButton;
        private Button sendDataButton;
        private Button retrieveTokenButton;
        private Button retrieveDataButton;
        private Button signActionButton;
        private Button sendActionButton;
        private Button verifyActionButton;
    }

    private static JsonParser parser = new JsonParser();
    private ViewHolder viewHolder = new ViewHolder();

    private NfcAdapter mNfcAdapter;

    private String userid = "1";
    private String type = "";

    private String requestMethod = "";
    private String requestUrl = "";
    private JsonObject requestData = null;
    private String requestToken = null;
    private JsonObject response = new JsonObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(Html.fromHtml("<font color='#FFFFFF'>NFC Transfer Example</font>"));

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessageCallback(MainActivity.this, MainActivity.this);
        }

        viewHolder.serverIp = (EditText) findViewById(R.id.serverIp);
        viewHolder.receivedData = (TextView) findViewById(R.id.receivedData);
        viewHolder.getPlainButton = (Button)findViewById(R.id.getPlain);
        viewHolder.getTokenButton = (Button)findViewById(R.id.getToken);
        viewHolder.getDataButton = (Button)findViewById(R.id.getData);
        viewHolder.sendPlainButton = (Button)findViewById(R.id.sendPlain);
        viewHolder.sendTokenButton = (Button)findViewById(R.id.sendToken);
        viewHolder.sendDataButton = (Button)findViewById(R.id.sendData);
        viewHolder.retrieveTokenButton = (Button)findViewById(R.id.retrieveToken);
        viewHolder.retrieveDataButton = (Button)findViewById(R.id.retrieveData);
        viewHolder.signActionButton = (Button)findViewById(R.id.signAction);
        viewHolder.sendActionButton = (Button)findViewById(R.id.sendAction);
        viewHolder.verifyActionButton = (Button)findViewById(R.id.verifyAction);

        viewHolder.getPlainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Get plain button pressed");
                type = "getPlain";
                requestMethod = "GET";
                requestUrl = "http://" + viewHolder.serverIp.getText().toString() + "/users/"+userid+"/creditCard";
                requestData = null;
                SendRequest sendRequest = new SendRequest();
                sendRequest.execute();
            }
        });

        viewHolder.getTokenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Get token button pressed");
                type = "getToken";
                requestMethod = "GET";
                requestUrl = "http://" + viewHolder.serverIp.getText().toString() + "/users/"+userid+"/paymentToken";
                requestData = null;
                SendRequest sendRequest = new SendRequest();
                sendRequest.execute();
            }
        });

        viewHolder.getDataButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Get data button pressed");
                type = "getData";
                requestMethod = "GET";
                requestUrl = "http://" + viewHolder.serverIp.getText().toString() + "/users/"+userid+"/encryptedCard";
                requestData = null;
                SendRequest sendRequest = new SendRequest();
                sendRequest.execute();
            }
        });

        viewHolder.signActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Sign action button pressed");
                type = "signAction";
                requestMethod = "PUT";
                requestUrl = "http://" + viewHolder.serverIp.getText().toString() + "/signAction";
                JsonObject requestObject = new JsonObject();
                requestObject.addProperty("action", "unlock");
                requestData = requestObject;
                SendRequest sendRequest = new SendRequest();
                sendRequest.execute();
            }
        });

        viewHolder.sendPlainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Send plain button pressed");
                mNfcAdapter.setNdefPushMessageCallback(MainActivity.this, MainActivity.this);
            }
        });

        viewHolder.sendTokenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Send token button pressed");
                mNfcAdapter.setNdefPushMessageCallback(MainActivity.this, MainActivity.this);
            }
        });

        viewHolder.sendDataButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Send data button pressed");
                mNfcAdapter.setNdefPushMessageCallback(MainActivity.this, MainActivity.this);
            }
        });

        viewHolder.sendActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Send action button pressed");
                mNfcAdapter.setNdefPushMessageCallback(MainActivity.this, MainActivity.this);
            }
        });

        viewHolder.retrieveTokenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Retrieve token button pressed");
                type = "retrieveToken";
                requestMethod = "GET";
                requestUrl = "http://" + viewHolder.serverIp.getText().toString() + "/users/"+userid+"/creditCard";
                requestData = null;
                requestToken = viewHolder.receivedData.getText().toString();
                SendRequest sendRequest = new SendRequest();
                sendRequest.execute();
            }
        });

        viewHolder.retrieveDataButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Retrieve data button pressed");
                type = "retrieveData";
                requestMethod = "PUT";
                requestUrl = "http://" + viewHolder.serverIp.getText().toString() + "/decryptCard";
                JsonObject requestObject = new JsonObject();
                requestObject.addProperty("encryptedCard", viewHolder.receivedData.getText().toString());
                requestData = requestObject;
                SendRequest sendRequest = new SendRequest();
                sendRequest.execute();
            }
        });

        viewHolder.verifyActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: Verify action button pressed");
                type = "verifyAction";
                requestMethod = "PUT";
                requestUrl = "http://" + viewHolder.serverIp.getText().toString() + "/verifyAction";
                JsonObject requestObject = new JsonObject();
                requestObject.addProperty("signature", viewHolder.receivedData.getText().toString());
                requestObject.addProperty("action", "unlock");
                requestData = requestObject;
                SendRequest sendRequest = new SendRequest();
                sendRequest.execute();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        String text = viewHolder.receivedData.getText().toString();
        Log.i(TAG, "createNdefMessage: Sending NFC message: " + text);
        return new NdefMessage(new NdefRecord[] { NdefRecord.createMime(
                "application/vnd.com.example.andrewpratt.exampleapp", text.getBytes()), NdefRecord.createApplicationRecord("com.example.andrewpratt.exampleapp")
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            processIntent(getIntent());
        }
    }

    private void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String nfcCode = new String(msg.getRecords()[0].getPayload());

        // set nfcCode to the view
        Log.i(TAG, "processIntent: nfcCode " + nfcCode);
        viewHolder.receivedData.setText(nfcCode);
    }

    private class SendRequest extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... unused) {
            Log.i(TAG, "sendRequest: [method]=" + requestMethod + " [url]="+requestUrl + " [requestData]="+requestData);

            InputStream in = null;

            try {
                URL url = new URL(requestUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod(requestMethod);
                if (type.equalsIgnoreCase("retrieveToken")) {
                    conn.setRequestProperty ("x-token", requestToken);
                } else if (requestMethod.equalsIgnoreCase("PUT") || requestMethod.equalsIgnoreCase("POST")) {
                    conn.setRequestProperty ("Content-Type", "application/json");
                    DataOutputStream dataOut = new DataOutputStream(conn.getOutputStream());
                    dataOut.writeBytes(requestData.toString());
                    dataOut.flush();
                    dataOut.close();
                }

                in = conn.getInputStream();
                response = parser.parse(IOUtils.toString(in, "UTF-8")).getAsJsonObject();
                Log.i(TAG, "Got response: " +response);
            } catch(Exception e){
                Log.e(TAG, "SendRequest: Error", e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            switch (type) {
                case "getToken":
                    viewHolder.receivedData.setText(response.getAsJsonPrimitive("token")
                            .toString()
                            .replaceAll("^\"|\"$", ""));
                    break;
                case "getData":
                    viewHolder.receivedData.setText(response.getAsJsonPrimitive("encryptedCard")
                            .toString()
                            .replaceAll("^\"|\"$", ""));
                    break;
                case "signAction":
                    viewHolder.receivedData.setText(response.getAsJsonPrimitive("signedAction")
                            .toString()
                            .replaceAll("^\"|\"$", ""));
                    break;
                case "retrieveToken":
                case "retrieveData":
                case "getPlain":
                case "verifyAction":
                    viewHolder.receivedData.setText(response.toString());
                    break;
            }
        }
    }
}
