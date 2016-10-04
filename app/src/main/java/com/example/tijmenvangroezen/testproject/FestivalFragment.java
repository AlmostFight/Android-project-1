package com.example.tijmenvangroezen.testproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by tijmenvangroezen on 03-10-16.
 */

public class FestivalFragment extends AppCompatDialogFragment
{
    private final String LOG_TAG = FestivalFragment.class.getSimpleName();

    private FestivalAdapter festivalAdapter;

    public FestivalFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.festivalfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.action)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        createConnection();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        festivalAdapter = new FestivalAdapter(
                getActivity(),
                R.layout.list_item_festival,
                R.id.listview_item_title,
                new ArrayList<FestivalEvent>()
        );

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_festivalevents);
        listView.setAdapter(festivalAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, String.valueOf(festivalAdapter));
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void createConnection()
    {
        String apiKey = "at5u4kjvcpeAPcTv";//your api key here
        String secretKey = "TOFUSbVwyHFQUJYaEEA13FYhFZv4WSI";//your secret key here
        String signature;

        final String FESTIVAL_BASE_URL =
                "http://api.edinburghfestivalcity.com";

        final String BASE_PATH = "events";
        final String FESTIVAL_PARAM = "festival";
        final String KEY_PARAM = "key";
        final String SIGNATURE_PARAM = "signature";
        final String CODE_PARAMETER = "code";
        final String PRETTY_PARAM = "pretty";
        final String SIZE = "size";

        //First construct the Uri without the signature
        Uri unsignedUri = Uri.parse(FESTIVAL_BASE_URL).buildUpon()
                .appendPath(BASE_PATH)
                .appendQueryParameter(FESTIVAL_PARAM, "science")
                .appendQueryParameter(PRETTY_PARAM, "1")
                .appendQueryParameter(SIZE, "25")
                .appendQueryParameter(KEY_PARAM, apiKey)
                .build();

        //Extract the part we need to generate a signature
        String unsignedQuery = unsignedUri.getPath() + "?" + unsignedUri.getQuery();

        try {
            signature = createFestivalSignature("HmacSHA1", unsignedQuery, secretKey);
            Log.d(LOG_TAG, signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            return;
        }

        final String signedUrl = unsignedUri.buildUpon()
                .appendQueryParameter(SIGNATURE_PARAM, signature)
                .build()
                .toString();

        Log.d(LOG_TAG, signedUrl);

        getData(signedUrl);
    }

    private void getData(String url)
    {
        final RequestQueue queque = RequestQueueSingleton.getInstance(getActivity()).getRequestQueue();

        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(FestivalEvent.Image[].class,
                                    new FestivalEvent.ImagesDeserializer())
                            .create();
                    FestivalEvent[] festivalEvent = gson.fromJson(response.toString(), FestivalEvent[].class);
                    for(FestivalEvent events : festivalEvent)
                    {
                        festivalAdapter.add(events);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(LOG_TAG, "Error fetching JSON data " + error.getMessage());
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();
                params.put("Accept", "application/json;ver=2.0");
                return params;
            }
        };
        RequestQueueSingleton.getInstance(getActivity()).addToRequestQueue(jsonRequest);
    }

    private String createFestivalSignature(String cryptoAlgorithm, String unsignedQuery,
                                           String secretKey)
            throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException
    {


        Mac mac = Mac.getInstance(cryptoAlgorithm);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
        mac.init(secret);
        byte[] digest = mac.doFinal(unsignedQuery.getBytes());

        //Convert digest to a ASCII hex string (courtesy of
        //http://stackoverflow.com/questions/15429257/how-to-convert-byte-array-to-hexstring-in-java)
        StringBuilder builder = new StringBuilder();
        for (byte b : digest)
        {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
