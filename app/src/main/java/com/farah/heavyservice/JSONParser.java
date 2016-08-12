package com.farah.heavyservice;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Georgi on 8/12/2016.
 */
public class JSONParser {
    static InputStream inputStream = null;
    static JSONObject jsonObj = null;
    static String jsonString = "";

    public JSONParser(){

    }

    // here we should consider both methods of an http Request : post and get
    public JSONObject makeHTTPRequest(String url, String method, List<NameValuePair> params){
        try{
            
        }
    }
}
