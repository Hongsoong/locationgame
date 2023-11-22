package com.example.projectapp;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {
    //서버 url 설정 부분(php 파일 연동)
    final static private String URL="http://sungi21.dothome.co.kr/Register1.php";
    private Map<String, String> map;

    public RegisterRequest(String userId, String userPass, String userName, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);

        map =new HashMap<>();
        map.put("userId", userId);
        map.put("userPass", userPass);
        map.put("userName", userName);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
