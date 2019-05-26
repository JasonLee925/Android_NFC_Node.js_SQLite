package com.example.jasonlee.nfc_demo;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private MifareClassic mfc;
    private String[][] mTechList;
    private StringBuilder ex_id = new StringBuilder();
    private final String URL = "http://192.168.0.175:8000";
    private TextView idView, responseView;
    private Button buttonPost, buttonGetLast, buttonGetWithID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            this.finish();
            return;
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        mIntentFilters = new IntentFilter[]{filter};
        mTechList = new String[][]{new String[]{MifareClassic.class.getName()}}; // 只處理 MifareClassic 的 tag
    }

    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent,
                mIntentFilters, mTechList);
    }

    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    private void initViews() {
        responseView = findViewById(R.id.tv_cardMsg);
        idView = findViewById(R.id.tv_tagID);
        buttonPost = findViewById(R.id.b_postCard);
        buttonGetLast = findViewById(R.id.b_getLastCard);
        buttonGetWithID = findViewById(R.id.b_listCardUID);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            return;
        }

        byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        ex_id = Converter.getHexString(myNFCID, myNFCID.length);
        idView.setText(ex_id); //只讀 tag 的 id
        responseView.setText("Tag is detected!");
    }


    // Onclick Button: Post card.
    public void bPostCardOnClick(View view) {
        HashMap data = new HashMap();
        data.put("uid", ex_id);

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST, URL + "/nfc/uid", new JSONObject(data), new Response.Listener<JSONObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String displayText = "";
                    JSONArray cards = response.getJSONArray("rows");
                    for (int i = 0; i < cards.length(); i++) {
                        JSONObject c = cards.getJSONObject(i);
                        displayText += "============================" + "\n" +
                                "uid:" + c.getString("uid") + "\n" +
                                "create_time:" + c.getString("create_time") + "\n" +
                                "uid:" + c.getString("update_time") + "\n" +
                                "update_time:" + c.getString("count") + "\n" +
                                "is_last:" + c.getString("is_last") + "\n";
                    }

                    responseView.setText(displayText);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseView.setText(error.getMessage());
                error.printStackTrace();
            }
        });

        Volley.newRequestQueue(MainActivity.this).add(postRequest);
    }

    // Onclick Button: Get the last card.
    public void bGetLastCardOnClick(View view) {
        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET, URL + "/nfc/lastuid", null, new Response.Listener<JSONObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String _res = response.getString("row")
                            .replace("{", "")
                            .replace("}", "");

                    String[] ary = _res.split(",");

                    String displayText = "";
                    for (String s : ary) {
                        displayText += "\n" + s;
                    }

                    responseView.setText(displayText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseView.setText(error.getMessage());
                error.printStackTrace();
            }
        });

        Volley.newRequestQueue(MainActivity.this).add(getRequest);
    }

    // Onclick Button: Get card with UID.
    public void bGetCardWithUID(View view) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, URL + "/nfc/cards/" + ex_id, null, new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String _res = response.getString("row")
                                    .replace("{", "")
                                    .replace("}", "");

                            String[] ary = _res.split(",");

                            String displayText = "";
                            for (String s : ary) {
                                displayText += "\n" + s;
                            }

                            responseView.setText(displayText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responseView.setText("Something wrong!");
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(MainActivity.this).add(jsonRequest);
    }
}

