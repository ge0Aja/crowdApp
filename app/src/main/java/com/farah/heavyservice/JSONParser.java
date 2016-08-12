package com.farah.heavyservice;

import android.util.Pair;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;


/**
 * Created by Georgi on 8/12/2016.
 */
public class JSONParser {
   /* static InputStream inputStream = null;
    static JSONObject jsonObj = null;
    static String jsonString = "";*/

    public JSONParser(){

    }
    // here we should consider both methods of an http Request : post and get

    public String makeHTTPPostRequest(String url,List<Pair<String,String>> params) {
        String output = "";
        DataOutputStream out;
        StringBuilder sb = new StringBuilder();
        try {
            URL useURL = new URL(url);
            //  if (method == "POST"){
            HttpURLConnection postUrlConnection = (HttpURLConnection) useURL.openConnection();
            postUrlConnection.setReadTimeout(10000);
            postUrlConnection.setConnectTimeout(15000);
            postUrlConnection.setRequestMethod("POST");
            postUrlConnection.setDoInput(true);
            postUrlConnection.setDoOutput(true);
            postUrlConnection.setUseCaches(false);
            postUrlConnection.setRequestProperty("Content-Type", "application/json");
            postUrlConnection.setRequestProperty("Host", "CrowdApp");
            postUrlConnection.connect();

            // create JSON OBJECT
            JSONObject newJsonObj = new JSONObject();
            newJsonObj = makeJsonObject(params);

            out = new DataOutputStream(postUrlConnection.getOutputStream());
            out.writeBytes(URLEncoder.encode(newJsonObj.toString(), "UTF-8"));
            out.flush();
            out.close();

            if (postUrlConnection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + postUrlConnection.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (postUrlConnection.getInputStream())));
            //String output;
            sb.append("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                sb.append(output);
                //System.out.println(output);
            }
            postUrlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public JSONObject makeJsonObject(List<Pair<String,String>> params){
            JSONObject jsonobj = new JSONObject();
        for (Pair<String,String > param : params)
                {
                    try {
                        jsonobj.put(param.first.toString(),param.second.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

        return jsonobj;
        }
    }

