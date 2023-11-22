package com.example.projectapp;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {
    final static private String URL="http://sungi21.dothome.co.kr/Login1.php";
    private Map<String, String> map;

    public LoginRequest(String userId, String userPass, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);

        map =new HashMap<>();
        map.put("userId", userId);
        map.put("userPass", userPass);

    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
