package com.coding.codingtest;

import android.app.Activity;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String DATA_URL="https://restcountries.eu/rest/v2/all";
    RequestQueue queue;
    ArrayList<CountryData> dataList;
    private static RecyclerView rv;
    mAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        fetchData();
    }

    private void initViews(){
        dataList=new ArrayList<>();
        rv=(RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchData(){
        queue= Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.GET, DATA_URL, new JSONArray(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {

                    int def=response.length();
                    for(int i=0;i<response.length();i++){
                        JSONObject obj=response.getJSONObject(i);
                        CountryData countryData=new CountryData();
                        countryData.setName(obj.getString("name"));
                        countryData.setCurrency(obj.getJSONArray("currencies").getJSONObject(0).getString("name"));
                        countryData.setLanguage(obj.getJSONArray("languages").getJSONObject(0).getString("name"));
                        dataList.add(countryData);
                    }
                    loadData();

                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "JSON Parsing Unsuccessful! ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Server Error! "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequest);

    }

    private void loadData(){
        adapter=new mAdapter(this,dataList);
        rv.setAdapter(adapter);
        Swiper  swiper = new Swiper(this, rv,adapter) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new Swiper.UnderlayButton("bomb Button",MainActivity.this));
            }
        };

    }

}
