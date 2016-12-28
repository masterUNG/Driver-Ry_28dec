package nakthon.soraya.driverry;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.regex.Pattern;

public class ServiceActivity extends FragmentActivity implements OnMapReadyCallback {
    //Explicit
    private GoogleMap mMap;
    private TextView nameTextView, phoneTextView, dateTextView, timeTextView;
    private ImageView imageView;
    private Button button;
    private String[] loginStrings;
    private MyConstant myConstant;
    private String[] jobString;
    private String phoneString;
    private LocationManager locationManager;
    private Criteria criteria;
    private double latADouble, lngADouble;
    private LatLng latLng;
    private int hourWaitStartAnInt, minusWaitStartInt,
            hourWaitEndAnInt, minusWaitEndAnInt;
    private boolean aBoolean = true;
    private int startTimeCountHour = 0;
    private int startTimeCountMinus = 0;
    private int endTimeCountHour, endTimeCountMinus, endTimeCountDay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_service_layout);

        //Bind WIdget
        nameTextView = (TextView) findViewById(R.id.textView3);
        phoneTextView = (TextView) findViewById(R.id.textView4);
        dateTextView = (TextView) findViewById(R.id.textView5);
        timeTextView = (TextView) findViewById(R.id.textView6);
        imageView = (ImageView) findViewById(R.id.imageView2);
        button = (Button) findViewById(R.id.button4);

        //Get Value From Intent
        loginStrings = getIntent().getStringArrayExtra("Login");
        Log.d("28decV2", "id_Passenger ==>" + loginStrings[0]);
        Log.d("28decV2", "time เวลานัดหมาย ==>" + loginStrings[0]);

        //Get Value From JSON
        myConstant = new MyConstant();
        GetJob getJob = new GetJob(ServiceActivity.this);
        getJob.execute(myConstant.getUrlGetJobWhereID());


        //Setup For Get Location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Button Controller
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //ค่าเริ่มต้นของ aBoolean มีค่า True แต่ถ้าคลิ๊กครั้งแรก จะมีค่า False
                if (aBoolean) {
                    //ก่อนออกเดินทาง

                    aBoolean = false;
                    button.setText(getResources().getString(R.string.start));

                    //Intent to PhotoActivity
                    Intent intent = new Intent(ServiceActivity.this, PhotoActivity.class);
                    intent.putExtra("id_job", jobString[0]);
                    intent.putExtra("phone_customer", phoneString);
                    startActivity(intent);

                } else {
                    //เริ่มเดินทาง หรือหยุดเวลา ที่จับ
                    Calendar calendar = Calendar.getInstance();
                    endTimeCountDay = calendar.get(Calendar.DAY_OF_MONTH);
                    endTimeCountHour = calendar.get(Calendar.HOUR_OF_DAY);
                    endTimeCountMinus = calendar.get(Calendar.MINUTE);

                    Log.d("28decV2", "d:HH:mm เวลาที่หยุดจับ" +
                            endTimeCountDay + ":" + endTimeCountHour + ":" + endTimeCountMinus);

                }   //if

                Log.d("28decV2", "aBoolean ==> " + aBoolean);


            }   //onClick
        });


    }   //Main Method

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //FOr Find Location by Network
        Location networkLocation = myFindLocation(LocationManager.NETWORK_PROVIDER);
        if (networkLocation != null) {
            latADouble = networkLocation.getLatitude();
            lngADouble = networkLocation.getLongitude();

        }

        //For find Location by GPS
        Location gpsLocation = myFindLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation != null) {
            latADouble = gpsLocation.getLatitude();
            lngADouble = gpsLocation.getLongitude();

        }

        Log.d("8novV1", "Lat ==> " + latADouble);
        Log.d("8novV1", "lug ==> " + lngADouble);

        Log.d("14novV2", "Resume Worked");

        afterReume();


        if (!aBoolean) {

            Log.d("14novV2", "Min ==>" + 0);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    //จุดที่เริ่มจับเวลา และส่ง SMS
                    Log.d("28decV1", "หน่วงเวลา 60 วินาทีเรียบร้อบแล้ว");
                    myCounterTime();
                    mySentSMS(phoneString);


                }   // run
            }, 60000);

        }   // if


    }   // onResume

    //คือเmethodที่ทำงาน หลังจาก ถ่ายรูปมิเตอร์ เรียบร้อยแล้ว

    private void afterReume() {



        try {

            //เช็คว่า มาก่อน หรือ หลังเวลานัด
            Calendar calendar = Calendar.getInstance();
            int intDay = calendar.get(Calendar.DAY_OF_MONTH);
            int intHour = calendar.get(Calendar.HOUR_OF_DAY);
            int intMinus = calendar.get(Calendar.MINUTE);
            Log.d("28decv2", "intHour ==> " + intHour);
            Log.d("28decV2", "intMinus ==> " + intMinus);
            Log.d("28decV2", "เวลานัดหมาย ==> " + jobString[5]);

            String[] timeStrings = jobString[5].split(Pattern.quote("."));
            Log.d("28decV2", "Hour ที่นัดหมาย ==> " + timeStrings[0]);
            Log.d("28decV2", "Minus ที่นัดหมาย ==> " + timeStrings[1]);

            String[] dateStrings = jobString[4].split(Pattern.quote("/"));
            if (intDay <= Integer.parseInt(dateStrings[0])) {

                if (Integer.parseInt(timeStrings[0]) <=23) {

                    if (intHour <=Integer.parseInt(timeStrings[0])) {

                        if (intMinus <=Integer.parseInt(timeStrings[1])) {
                            Log.d("28decV2", "มาก่อน หรือ ตรงเวลา");

                            startTimeCountHour = Integer.parseInt(timeStrings[0]);
                            startTimeCountMinus = Integer.parseInt(timeStrings[1]) + 1;

                        } else
                            Log.d("28decV2", "มาสาย");
                        startTimeCountHour = intHour;
                        startTimeCountMinus = intMinus + 1;

                    } else {
                        Log.d("28decV2", "มาสาย");
                        startTimeCountHour = intHour;
                        startTimeCountMinus = intMinus + 1;
                    }

                }   // if1

            } else {
                Log.d("28decV2", "มาสาย");
                startTimeCountHour = intHour;
                startTimeCountMinus = intMinus + 1;
            }   // if Day

            // นี่คือเวลาที่เริ่ม จับ
            Log.d("28decV2", "เวลาที่เริ่มจับ ==> " + startTimeCountHour + ":" + startTimeCountMinus);


        } catch (Exception e) {
            e.printStackTrace();
        }





    }   // afterResume

    private void mySentSMS(String phoneString) {

        Log.d("28decV1", "phoneCustomer ==> " + phoneString);

//       Uri uri = Uri.parse("smsto" + phoneString);
//       Intent intent = new Intent(Intent.ACTION_SENDTO);
//       intent.setData(uri);
//       intent.putExtra("sms_body", "Test by MasterUNG");
//       startActivity(intent);

//       SmsManager smsManager = SmsManager.getDefault();
//       smsManager.sendTextMessage(phoneString, null, "Test Master", null null);


    }   //mySentSMS

    //ทำหน้าที่จับเวลา ที่ต้องรอลูกค้า
    private void myCounterTime() {

        //Get Time WaitStart
        Calendar calendar = Calendar.getInstance();
        hourWaitStartAnInt = calendar.get(Calendar.HOUR_OF_DAY);
        minusWaitStartInt = calendar.get(Calendar.MINUTE);
        Log.d("14novV2", "HrStarts ==>" + hourWaitStartAnInt);
        Log.d("14novV2", "MinStart ==>" + minusWaitStartInt);

    }

    @Override
    protected void onStop() {
        super.onStop();

        locationManager.removeUpdates(locationListener);

    }

    public Location myFindLocation(String strProvider) {

        Location location = null;

        if (locationManager.isProviderEnabled(strProvider)) {

            locationManager.requestLocationUpdates(strProvider, 1000, 10, locationListener);
            location = locationManager.getLastKnownLocation(strProvider);
        }

        return location;
    }

    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            latADouble = location.getLatitude();
            lngADouble = location.getLongitude();

        }   //onLocationChange

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    private class GetJob extends AsyncTask<String, Void, String> {


        //Explicit
        private Context context;

        public GetJob(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {

            try {

                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = new FormEncodingBuilder()
                        .add("isAdd", "true")
                        .add("ID_passenger", loginStrings[0])
                        .build();
                Request.Builder builder = new Request.Builder();
                Request request = builder.url(strings[0]).post(requestBody).build();
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }


        }   //doInBack

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("7novV1", "Result ==>" + s);

            try {

                JSONArray jsonArray = new JSONArray(s);

                String[] columnStrings = myConstant.getJobStrings();

                jobString = new String[columnStrings.length];

                for (int i = 0; i < columnStrings.length; i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    jobString[i] = jsonObject.getString(columnStrings[i]);
                    Log.d("7novV2", "jobString(" + i + ") ==> " + jobString[i]);

                }   // for

                //Create Marker Start
                LatLng startLatlng = new LatLng(Double.parseDouble(jobString[7]),
                        Double.parseDouble(jobString[8]));
                mMap.addMarker(new MarkerOptions()
                        .position(startLatlng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.nobita48)));

                //Create Marker End
                LatLng endlatLng = new LatLng(Double.parseDouble(jobString[10]),
                        Double.parseDouble(jobString[11]));
                mMap.addMarker(new MarkerOptions()
                        .position(endlatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bird48)));


                //Show Text
                GetPassenger getPassenger = new GetPassenger(context, jobString);
                getPassenger.execute(myConstant.getUrlGetPassengerWhereID());


            } catch (Exception e) {
                Log.d("7novV2", "e ==>" + e.toString());
            }

        }   // onPost

    }   //GetJob Class


    private class GetPassenger extends AsyncTask<String, Void, String> {

        //Explicit
        private Context context;
        private String[] resultStrings;

        public GetPassenger(Context context, String[] resultStrings) {
            this.context = context;
            this.resultStrings = resultStrings;
        }

        @Override
        protected String doInBackground(String... strings) {

            try {

                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = new FormEncodingBuilder()
                        .add("isAdd", "true")
                        .add("id", resultStrings[1])
                        .build();
                Request.Builder builder = new Request.Builder();
                Request request = builder.url(strings[0]).post(requestBody).build();
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();

            } catch (Exception e) {
                Log.d("7novV3", "e ==>" + e.toString());
                return null;
            }

        }   //doInBack

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("7novV3", "Passenger ==>" + s);

            try {

                JSONArray jsonArray = new JSONArray(s);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                nameTextView.setText(jsonObject.getString("Name"));
                phoneString = jsonObject.getString("Phone");
                phoneTextView.setText("Phone = " + phoneString);
                dateTextView.setText("วันที่ไปรัย = " + jobString[4]);
                timeTextView.setText("เวลาที่ไปรับ = " + jobString[5]);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("7novV3", "ClickPhone = " + phoneString);

                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:=" + phoneString));
                        startActivity(intent);

                    }   //onClick
                });

            } catch (Exception e) {
                Log.d("7novV3", "e onPost ==> " + e.toString());
            }


        }   // onPost
    } // GetPassenger Class


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {

            latLng = new LatLng(latADouble, lngADouble);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

            //Create Marker Driver
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.humen)));


        } catch (Exception e) {
            Toast.makeText(ServiceActivity.this, "ไม่สามารถหาพิกัด", Toast.LENGTH_SHORT).show();

        }


    }  //onMapReady

}  //Main Class
