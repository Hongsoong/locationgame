package com.example.projectapp;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class MapRequest extends StringRequest {
    final static private String URL="http://sungi21.dothome.co.kr/mainapp.php";
    private Map<String, String> map;

    public MapRequest(String userLocationLa, String userIngame, String userIdentify, String userLocationLo,String userId){
        super(Method.POST, URL,null,null);

        map = new HashMap<>();
        map.put("userLocationLa",userLocationLa);
        map.put("userIngame", userIngame);
        map.put("userIdentify", userIdentify);
        map.put("userLocationLo",userLocationLo);
        map.put("userId",userId);


    }

    @Nullable
    @Override
    protected Map<String,String> getParams() throws AuthFailureError {
        return map;
    }
}
