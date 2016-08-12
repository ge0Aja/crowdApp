package com.farah.heavyservice;

import android.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public String makeHTTPGetRequest(String url){
        String output ="";
        StringBuilder sb = new StringBuilder();
        HttpURLConnection getUrlConnection = null;
        try{
            URL useURL = new URL(url);
            getUrlConnection = (HttpURLConnection) useURL.openConnection();
            getUrlConnection.setReadTimeout(10000);
            getUrlConnection.setConnectTimeout(15000);
            getUrlConnection.setRequestMethod("GET");
            getUrlConnection.setDoInput(true);
            getUrlConnection.setDoOutput(true);
            getUrlConnection.setUseCaches(false);

            int responseCode = getUrlConnection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (getUrlConnection.getInputStream())));
                //String output;
                sb.append("Output from Server .... \n");
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                    //System.out.println(output);
                }
            }
            getUrlConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (getUrlConnection != null)
                getUrlConnection.disconnect();
        }

        return sb.toString();
    }
    public String makeHTTPPostRequest(String url,List<Pair<String,String>> params) throws IOException {
        String output = "";
        DataOutputStream out =null;
        StringBuilder sb = new StringBuilder();
        HttpURLConnection postUrlConnection = null;
        try {
            URL useURL = new URL(url);
            //  if (method == "POST"){
            postUrlConnection = (HttpURLConnection) useURL.openConnection();
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
          //  JSONObject newJsonObj = new JSONObject();
            JSONObject newJsonObj = makeJsonObject(params);

            out = new DataOutputStream(postUrlConnection.getOutputStream());
            out.writeBytes(URLEncoder.encode(newJsonObj.toString(), "UTF-8"));
            out.flush();


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
        }finally {
            if(postUrlConnection != null)
                postUrlConnection.disconnect();
            if(out != null)
                out.close();
        }
        return sb.toString();
    }


    public JSONObject makeJsonObject(List<Pair<String,String>> params){
            JSONObject jsonobj = new JSONObject();
        for (Pair<String,String > param : params)
                {
                    try {
                        jsonobj.put(param.first,param.second);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

        return jsonobj;
        }
    }

