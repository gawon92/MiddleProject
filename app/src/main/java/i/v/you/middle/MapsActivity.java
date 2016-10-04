package i.v.you.middle;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tsengvn.typekit.TypekitContextWrapper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import static android.R.id.edit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LinearLayout searchLinear;
    Button locationPlusBtn;
    EditText addressEt;
    int resultCount=0;
    String data="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        searchLinear = (LinearLayout) findViewById(R.id.SearchLinear);
        locationPlusBtn=(Button) findViewById(R.id.locationPlusBtn);
        locationPlusBtn.bringToFront();

        addressEt = (EditText) findViewById(R.id.addressEt);



    }


    @Override
    protected void attachBaseContext(Context newBase) { //Application 클래스 (폰트 적용)
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void onLocationPlus(View view){  //위치 추가 버튼 눌렀을 때 발생하는 method
        locationPlusBtn.setVisibility(View.GONE);

        addressEt.setText(null);
        searchLinear.setVisibility(View.VISIBLE);
        searchLinear.bringToFront();
    }

    public void onSearch(View view){    // 주소로 위치 입력받아서 마커찍는 method

        String location = addressEt.getText().toString();
        String uriLocation="";
        StringTokenizer stringTokenizer=new StringTokenizer(location," ");
        while(stringTokenizer.hasMoreTokens()){
            if(stringTokenizer.countTokens()>1) {
                uriLocation += stringTokenizer.nextToken()+"+";
            }else{
                uriLocation += stringTokenizer.nextToken();
            }
        }
        final String uriLocationStr=uriLocation;

//        Log.i("결과",uriLocation);

        new Thread() {  //Google Place Web Server에 Uri 연결하기 위해 Thread 실행 (UI Thread에서 실행 불가하기 때문)
            public void run() {
                String line = getPlaceInfo(uriLocationStr);

                Bundle bun = new Bundle();
                bun.putString("Place_Info", line);
                Message msg = handler.obtainMessage();
                msg.setData(bun);
                handler.sendMessage(msg);

            }
        }.start();


    searchLinear.setVisibility(View.GONE);
        locationPlusBtn.setVisibility(View.VISIBLE);

//            LatLng latLng = new LatLng(xml에서 가져온 위도,xml에서 가져온 경도);
//            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
//            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//        }
    }

    private String getPlaceInfo(String location){   //Google Place Web Server에 Uri를 통해 접근한 후 결과값을 xml 파일 형식으로 가져오는 method
//        String line="";

//        URL url =null;
//        HttpURLConnection http = null;
//        InputStreamReader isr = null;
//        BufferedReader br = null;
//
//        try{
//            //uri 주소 +로 연결되어야함 예를들면 성남+대원빌라 이런식으로
//            url = new URL("https://maps.googleapis.com/maps/api/place/textsearch/xml?query="+location+"&key=AIzaSyDbq-mtJOlLU3SEjaPFSgfWYhF_V8q3qBc");
//            http = (HttpURLConnection) url.openConnection();
//            http.setConnectTimeout(3*1000);
//            http.setReadTimeout(3*1000);
//
//
//            isr = new InputStreamReader(http.getInputStream());
//            br = new BufferedReader(isr);
//
//            String str = null;
//            while ((str = br.readLine()) != null) {
//                line += str + "\n";
//            }
//
//        }catch(Exception e){
//            Log.e("Exception", e.toString());
//        }finally{
//            if(http != null){
//                try{http.disconnect();}catch(Exception e){}
//            }
//            if(isr != null){
//                try{isr.close();}catch(Exception e){}
//            }
//            if(br != null){
//                try{br.close();}catch(Exception e){}
//            }
//        }



        StringBuffer buffer=new StringBuffer();
        String encodingLocation = URLEncoder.encode(location); //한글의 경우 인식이 안되기에 utf-8 방식으로 encoding..
        String queryUrl="https://maps.googleapis.com/maps/api/place/textsearch/xml?query="+location+"&key=AIzaSyDbq-mtJOlLU3SEjaPFSgfWYhF_V8q3qBc";

        try {
            URL url= new URL(queryUrl); //문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is= url.openStream();  //url위치로 입력스트림 연결

            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") );  //inputstream 으로부터 xml 입력받기

            String tag;
            xpp.next();

            int eventType= xpp.getEventType();
            while( eventType != XmlPullParser.END_DOCUMENT ){

                switch( eventType ){
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("start NAVER XML parsing...\n\n");
                        break;

                    case XmlPullParser.START_TAG:

                        tag= xpp.getName();    //테그 이름 얻어오기

                        if(tag.equals("result")){ // 첫번째 검색결과
                            resultCount++;
                        }
                        else if(tag.equals("name")){
                            buffer.append("업소명 : ");
                            xpp.next();
                            buffer.append(xpp.getText()); //title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");          //줄바꿈 문자 추가
                        }else if(tag.equals("formatted_address")){
                            buffer.append("위치 : ");
                            xpp.next();
                            buffer.append(xpp.getText()); //category 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");          //줄바꿈 문자 추가
                        }else if(tag.equals("lat")){
                            buffer.append("위도 :");
                            xpp.next();
                            buffer.append(xpp.getText()); //description 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");          //줄바꿈 문자 추가
                        }else if(tag.equals("lng")){
                            buffer.append("경도 :");
                            xpp.next();
                            buffer.append(xpp.getText()); //telephone 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");          //줄바꿈 문자 추가
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        tag= xpp.getName();    //테그 이름 얻어오기
                        if(tag.equals("result")) buffer.append("\n"); // 첫번째 검색결과종료..줄바꿈
                        break;
                }
                eventType= xpp.next();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ""+resultCount+" "+buffer.toString();
    }

    Handler handler = new Handler() {   // Thread Handler
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String line = bun.getString("Place_Info");
            Log.i("결과",line);
        }
    };


    private void setUpMapIfNeeded() {
        if(mMap == null){
            mMap =((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            if(mMap != null){
                onMapReady(mMap);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng ajouUniv = new LatLng(37.2851815, 127.045332);
        mMap.addMarker(new MarkerOptions().position(ajouUniv).title("산학원"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ajouUniv,14));
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
//            mMap.setMyLocationEnabled(true);  // My Location 버튼 위치 안옮겨져서 비활성화 시킴. 버튼 새로만들어서 My Location 기능 구현할 것. btn 눌렀을 때 GPS 안켜져있으면 키라는 팝업창 만들 것.
//            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            // Show rationale and request permission.
        }
    }

}
