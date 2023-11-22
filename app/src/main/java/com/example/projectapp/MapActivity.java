package com.example.projectapp;





import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 100; // 0.1초

    // OnRequestPermissionsResultCallback에서 수신된 결과에서 ActivityCompat.OnRequestPermissionsResultCallback를 사용한 퍼미션 요청을 구별하기 위함
    private static final int PERMISSION_REQUEST_CODE = 100;
    boolean needRequest = false;

    // 앱을 실행하기 위해 필요한 퍼미션 정의
    String[] REQUIRED_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest; // 주의
    private Location location;

    private View mLayout; // snackbar 사용하기 위함.

    private TextView tv_id,tv_pass,tv_identify;

    private Button btn_logout;


    Random rn =new Random();
    int r = rn.nextInt(2);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        tv_identify=findViewById(R.id.tv_identify);
        tv_id=findViewById(R.id.tv_id);
        tv_pass=findViewById(R.id.tv_pass);
        btn_logout=findViewById(R.id.btn_logout);
//버튼을 누르면 데이터 베이스 초기화 및 로그인 페이지로 이동
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();

                String userId = intent.getStringExtra("userId");
                String userLocationLa= "NULL";
                String userIngame=String.valueOf(0);
                String userIdentify = "NULL";
                String userLocationLo = "NULL";

                MapRequest mapRequest =new MapRequest(userLocationLa, userIngame, userIdentify, userLocationLo, userId);
                RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
                queue.add(mapRequest);

                Intent intent2 = new Intent(MapActivity.this, LoginActivity.class);
                startActivity(intent2);
                Toast.makeText(getApplicationContext(),"로그아웃을 하였습니다.",Toast.LENGTH_SHORT).show();

            }
        });

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        String userPass = intent.getStringExtra("userPass");
        String userLocationLa= String.valueOf(1);
        String userIngame=String.valueOf(1);
        String userIdentify = String.valueOf(r);
        String userLocationLo = String.valueOf(1);



        tv_id.setText(userId);
        tv_pass.setText(userPass);

        if(r==1) {
            tv_identify.setText("당신은 !!킬러!! 입니다 ");
        } else {
            tv_identify.setText("당신은 일반인 입니다 ");
        }

        MapRequest mapRequest =new MapRequest(userLocationLa, userIngame, userIdentify, userLocationLo, userId);
        RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
        queue.add(mapRequest);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
//데이터 베이스 update 기능 추가 2023-10-30 마지막 update 새로 위치정보만 업데이트 하는 request를 만든 후 특정 칼럼 위치정보만 업데이트하는 기능 구현
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Log.d(TAG, "onMapReady: 들어옴 ");

        mMap = googleMap;

        // 지도의 초기위치 이동
        setDefaultLocation();

        // 런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 확인합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            startLocationUpdates(); // 3. 위치 업데이트 실행
        } else {
            // 2. 퍼미션 요청을 허용한 적 없다면 퍼미션 요청하기
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSION[0])) {
                // 요청 진행하기 전에 퍼미션이 왜필요한지 설명
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // 사용자에게 퍼미션 요청, 요청 결과는 onRequestPermisionResult에서 수신
                        ActivityCompat.requestPermissions(MapActivity.this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 사용자가 퍼미션 거부를 한적이 없는 경우 퍼미션 요청을 바로 함.
                // 요청 결과는 onRequestPermissionResult에서 수신된다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Log.d(TAG, "onMapClick: ");
            }
        });


        MarkerOptions markerOptions = new MarkerOptions();

        Intent intent = getIntent();

        String random = intent.getStringExtra("r");
        if (Objects.equals(random, "")){
            r=0;
            tv_identify.setText("당신은 일반인 입니다 ");
        }

//미션지 마크 추가 하기.
        if(r==0) {

            LatLng mission1 = new LatLng(37.4219988, -122.090);
            mMap.addMarker(markerOptions
                    .position(mission1)
                    .title("mission")
                    .snippet("미션을 받으세용")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

            );
            LatLng mission2 = new LatLng(37.4199988, -122.090);
            mMap.addMarker(markerOptions
                    .position(mission2)
                    .title("mission")
                    .snippet("미션을 받으세용")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

            );
            LatLng mission3 = new LatLng(37.4187988, -122.085);
            mMap.addMarker(markerOptions
                    .position(mission3)
                    .title("mission")
                    .snippet("미션을 받으세용")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

            );
            LatLng testuser1 = new LatLng(37.4187731,-122.087);
            mMap.addMarker(markerOptions
                    .position(testuser1)
                    .title("user")
                    .snippet("게임 중인 플레이어 입니다.")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            );
            LatLng testuser2 = new LatLng(37.4187501,-122.0887);
            mMap.addMarker(markerOptions
                    .position(testuser2)
                    .title("user")
                    .snippet("게임 중인 플레이어 입니다.")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            );
        }
        else {
            LatLng testuser1 = new LatLng(37.4187731,-122.087);
            mMap.addMarker(markerOptions
                    .position(testuser1)
                    .title("user")
                    .snippet("게임 중인 플레이어 입니다."+"sun12345")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );
            LatLng testuser2 = new LatLng(37.4187501,-122.0887);
            mMap.addMarker(markerOptions
                    .position(testuser2)
                    .title("user")
                    .snippet("게임 중인 플레이어 입니다.")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            );
        }

        Context context = this;

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {


            Intent intent = getIntent();
            String userId=intent.getStringExtra("userId");
            String userPass=intent.getStringExtra("userPass");


            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                String markerId = marker.getId();
                Toast.makeText(MapActivity.this, "정보창 클릭 Marker ID : "+markerId, Toast.LENGTH_SHORT).show();

                if(r==1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("일반유저");
                    builder.setMessage("유저를 죽이십시요");
                    builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setNegativeButton("kill", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MapActivity.this, "사용자를 죽였습니다.",Toast.LENGTH_SHORT).show();
                            marker.remove();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

                if(r==0&&(Objects.equals(markerId,"m1") || Objects.equals(markerId,"m2") || Objects.equals(markerId,"m3"))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("미션 정보");
                    builder.setMessage("술래에 관련한 힌트를 받기 위해 미션 수행 버튼을 클릭 하세요." +
                            "행운이 당신과 함께 할 것 입니다. ");
                    builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setNegativeButton("미션 수행", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //unity 엔진과 연동X, HTML JS를 사용한 미니게임

                            Intent intent = new Intent(MapActivity.this, GameActivity.class);
                            intent.putExtra("userId", userId);
                            intent.putExtra("userPass", userPass);
                            intent.putExtra("userr", r);

                            startActivity(intent);
                            marker.remove();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {

            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {

                Intent intent = getIntent();
                String username = intent.getStringExtra("userId");
                location = locationList.get(locationList.size() - 1);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = username;
                String markerSnippet = "위도 :" + String.valueOf(location.getLatitude()) + "경도 :" +
                        String.valueOf(location.getLongitude());


                setCurrentLocation(location, markerTitle, markerSnippet);

                mCurrentLocation = location;
                //위,경도 데이터 베이스 매초 업데이트

                String userId = intent.getStringExtra("userId");
                String userLocationLa= String.valueOf(location.getLatitude());
                String userIngame=String.valueOf(1);
                String userIdentify = String.valueOf(r);
                String userLocationLo = String.valueOf(location.getLongitude());

                MapRequest mapRequest =new MapRequest(userLocationLa, userIngame, userIdentify, userLocationLo, userId);
                RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
                queue.add(mapRequest);

            }
        }

    };


    private String getCurrentAddress(LatLng currentPosition) {
        // 지오코더 gps를 주소로 변환

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    currentPosition.latitude,
                    currentPosition.longitude,
                    1
            );
        } catch (IOException ioException) {
            // 네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용 불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    private void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) {
            currentMarker.remove();
        }

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }

    private void startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            showDiologForLocationServiceSetting();

        } else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates: 퍼미션 없음");
                return;
            }

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            if (checkPermission()) {
                mMap.setMyLocationEnabled(true);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart: ");

        if (checkPermission()) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);


            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "startLocationUpdates: 퍼미션 없음");
            return false;
        } else {

            return true;

        }
    }

    private void showDiologForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다. 위치설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);

        return
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void setDefaultLocation() {

        // 기본 위치
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치 정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요";

        if (currentMarker != null) {
            currentMarker.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }


}
