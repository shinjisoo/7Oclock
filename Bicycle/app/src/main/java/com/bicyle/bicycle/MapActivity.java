package com.bicyle.bicycle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {
    LocationManager lm;
    Context context = this;
    TMapView tmapview = null;       //tmap
    private double latitude;        //내 위치
    private double longitude;       //내 위치
    private double dest_lat;        //도착지 위치
    private double dest_long;       //도착지 위치
    public int state_flag_bit = 0; //0일때는 지도 검색 1일 때 경로탐색
    public static final int search_popup = 1;
    public static final String PIN_SEARCH = "search";
    public static final String PIN_SOURCE = "source";
    public static final String PIN_DEST = "dest";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mapview);
        final LinearLayout search_bar_layout = (LinearLayout) findViewById(R.id.search_bar_layout);
        final LinearLayout route_bar_layout = (LinearLayout)  findViewById(R.id.route_bar_layout);
        final TMapData tmapdata = new TMapData();
        final EditText search = (EditText)findViewById(R.id.map_search);
        final Button enter = (Button)findViewById(R.id.map_enter);
        final Button mygps = (Button)findViewById(R.id.map_gps);
        final Button route = (Button)findViewById(R.id.map_route);
        final Button findroute_enter = (Button) findViewById(R.id.findroute_enter);
        final EditText source_edt = (EditText)findViewById(R.id.source_edt);
        final EditText dest_edt = (EditText)findViewById(R.id.dest_edt);


        //티맵 설정
        tmapview = new TMapView(this);
        linearLayout.addView(tmapview);
        tmapview.setSKTMapApiKey("651558bc-d5a6-4dd3-9e96-524bfc2d59b8");
        tmapview.setCompassMode(true);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapview.setSightVisible(true);

        setGps();
        setMyGps();
        tmapview.setCenterPoint(longitude,latitude);

        //내위치 버튼
        mygps.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                if(state_flag_bit==0) {     //지도 검색이었을 때 내위치 버튼
                    setMyGps();
                    tmapview.setCenterPoint(longitude, latitude);
                }
            }
        });

        //출발지에서 내위치 버튼 눌렀을 때
        source_edt.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean gainFocus){
                if(gainFocus){
                    mygps.setOnClickListener(new Button.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            source_edt.setText("내위치");
                            //키보드 내리기
                            InputMethodManager mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            mInputMethodManager.hideSoftInputFromWindow(source_edt.getWindowToken(), 0);
                        }
                    });
                }
            }
        });

        //도착지에서 내위치 버튼 눌렀을 때
        dest_edt.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean gainFocus){
                if(gainFocus){
                    mygps.setOnClickListener(new Button.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            dest_edt.setText("내위치");
                            //키보드 내리기
                            InputMethodManager mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            mInputMethodManager.hideSoftInputFromWindow(dest_edt.getWindowToken(), 0);
                        }
                    });
                }
            }
        });

        //경로탐색 & 지도탐색 버튼
        route.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                if(state_flag_bit==0) {
                    search_bar_layout.setVisibility(View.GONE);
                    route_bar_layout.setVisibility(View.VISIBLE);
                    findroute_enter.setVisibility(View.VISIBLE);
                    route.setText("지도 검색");
                    tmapview.removeAllMarkerItem();
                    state_flag_bit=1;
                }
                else if(state_flag_bit==1){
                    search_bar_layout.setVisibility(View.VISIBLE);
                    route_bar_layout.setVisibility(View.GONE);
                    findroute_enter.setVisibility(View.INVISIBLE);
                    route.setText("경로 탐색");
                    tmapview.removeAllMarkerItem();
                    state_flag_bit=0;
                }
            }
        });

        //검색 했을 시
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                final String temp = editable.toString();
                //enter를 눌렀을때
                enter.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        search.setText(null);
                        search.setHint("검색");
                        searchMapData(temp);
                        //키보드 내리기
                        InputMethodManager mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        mInputMethodManager.hideSoftInputFromWindow(search.getWindowToken(), 0);

                    }
                });
            }
        });
    }
    //gps 위치에 관한 리스너

    //gps 위치에 관한 리스너
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            if (location != null) {

                if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    tmapview.setLocationPoint(longitude, latitude);
                    //Toast.makeText(MainActivity.this, "[Gps_Provider]gps위치 리스너\n" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                    Log.d("test", "[Gps_Provider]gps위치 리스너\n" + latitude + " " + longitude);
//                    tmapview.setTrackingMode(true);  //trackingmode는 한번만?
//                    tmapview.setCenterPoint(longitude, latitude); //윗놈이랑 차이를모르겟고..
                }
                else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    tmapview.setLocationPoint(longitude, latitude);
                    //Toast.makeText(MainActivity.this, "[NETWORK_PROVIDER]gps위치 리스너\n" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                    Log.d("test", "[NETWORK_PROVIDER]gps위치 리스너\n" + latitude + " " + longitude);
//                    tmapview.setTrackingMode(true);
//                    tmapview.setCenterPoint(longitude, latitude);
                }


            }

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    //현재 위치받기
    public void setGps() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


        //locationUpdates 는 한번만 등록되어야함 ?
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자(실내에선 NETWORK_PROVIDER 권장)
                1000, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // GPS_PROVIDER 권장
                1000, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
    }

    //Popup Activity로 데이터 전송
    public void mOnPopupClick(String msg, int requestCode){
        if(requestCode==search_popup) {
            Intent intent = new Intent(this, MapPopupActivity.class);
            intent.putExtra("data", msg);
            startActivityForResult(intent, requestCode);
        }
    }

    //Popup Activity로부터 데이터 받기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode==RESULT_OK){
            String result = data.getStringExtra("result");
            if (!result.equals("OK")) {
                Log.d("test1_lat", String.valueOf(dest_lat) + " " + String.valueOf(dest_long));
                findRoute();
            }
        }
    }

    //시작지를 내위치로 한 경로탐색
    public void findRoute(){
        TMapPoint tMapPointStart = new TMapPoint(latitude, longitude);
        TMapPoint tMapPointEnd = new TMapPoint(dest_lat, dest_long);

        try {
            TMapPolyLine tMapPolyLine = new TMapData().findPathData(tMapPointStart, tMapPointEnd);
            tMapPolyLine.setLineColor(Color.BLUE);
            tMapPolyLine.setLineWidth(2);
            tmapview.addTMapPolyLine("Line1", tMapPolyLine);

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //지도 데이터 검색
    public void searchMapData(String temp){
        TMapData tmapdata = new TMapData();
        tmapdata.findAllPOI(temp , new TMapData.FindAllPOIListenerCallback() {
            @Override
            //지역 검색
            public void onFindAllPOI(ArrayList poiItem) {
                //검색결과가 있을 때
                if(poiItem.size()!=0) {
                    TMapPOIItem search_place = (TMapPOIItem) poiItem.get(0);
                    double temp_lat = search_place.getPOIPoint().getLatitude();
                    double temp_long = search_place.getPOIPoint().getLongitude();
                    tmapview.setCenterPoint(temp_long, temp_lat);

                    //핀꽂기
                    makePin(temp_lat,temp_long,search_place, PIN_SEARCH);


                    mOnPopupClick("검색결과: " + search_place.getPOIName().toString() + "\n"
                            + "주소: " + search_place.getPOIAddress().replace("null",""),search_popup);

                }
                //검색결과가 없을 때
                else {
                    mOnPopupClick("해당 결과가 없습니다.",1);
                }

                                /*          example
                                for(int i = 0; i < poiItem.size(); i++) {
                                    TMapPOIItem  item = (TMapPOIItem) poiItem.get(i);

                                    Log.d("POI Name: ", item.getPOIName().toString() + ", " +
                                            "Address: " + item.getPOIAddress().replace("null", "")  + ", " +
                                            "Point: " + item.getPOIPoint().toString());
                                }
                                */
            }
        });
    }

    public void makePin(double temp_lat, double temp_long, TMapPOIItem search_place, String pin_id){
        //핀꽂기
        TMapPoint dest_point = new TMapPoint(temp_lat, temp_long);
        TMapMarkerItem dest_marker = new TMapMarkerItem();
        dest_marker.setTMapPoint(dest_point);
        dest_marker.setName(search_place.getPOIName().toString());
        dest_marker.setVisible(TMapMarkerItem.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin);
        dest_marker.setIcon(bitmap);
        dest_marker.setCanShowCallout(true);
        dest_marker.setCalloutTitle(search_place.getPOIName().toString());
        dest_marker.setCalloutSubTitle(search_place.getPOIAddress().replace("null",""));
        tmapview.addMarkerItem(pin_id,dest_marker);

    }

    public void setMyGps()
    {
        String locationProvider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation = lm.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {

            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
            //Toast.makeText(MainActivity.this, "[수동gps]gps위치 리스너\n" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
            Log.d("test", "[수동gps]gps위치 리스너\n" + latitude + " " + longitude);
        }

    }
}
