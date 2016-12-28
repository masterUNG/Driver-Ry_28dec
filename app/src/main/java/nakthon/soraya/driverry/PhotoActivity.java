package nakthon.soraya.driverry;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jibble.simpleftp.SimpleFTP;

import java.io.File;

public class PhotoActivity extends AppCompatActivity {

    //Explicit
    private ImageView takePhotoImage, showPhotoImageView;
    private Uri uri;
    private EditText editText;
    private Button button;
    private String meterString, imagePathString, imageNameString;
    private boolean aBoolean = true;
    private String idjobString;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        //Bind Widget
        showPhotoImageView = (ImageView) findViewById(R.id.imageView4);
        takePhotoImage = (ImageView) findViewById(R.id.imageView3);
        editText = (EditText) findViewById(R.id.editText3);
        button = (Button) findViewById(R.id.button3);

        //Get Value From Intent
        idjobString = getIntent().getStringExtra("id_job");
        Log.d("14novV1", "idJob ==> " + idjobString);

        //Buttom Controller
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              //Get Value from Edit Text
                meterString = editText.getText().toString().trim();

                //Check Space
                if (meterString.equals("")) {
                    //Have Space
                    MyAlert myAlert = new MyAlert(PhotoActivity.this,R.drawable.bird48,
                            "ยังไม่ได้กรอกค่าโดยสาร", "กรุณากรอก ค่าโดยสาร ด้วยค่ะ");
                    myAlert.myDialog();

                } else if (aBoolean) {
                    //Non Take Photo
                    MyAlert myAlert = new MyAlert(PhotoActivity.this, R.drawable.rat48,
                            "ยังไม่ได้ถ่ายรูปมิเตอร์", "กรุณาถ่ายรูปรูปมิเตอร์ด้วยค่ะ");
                    myAlert.myDialog();

                } else {
                    //Delay 1 Min

                    upLoadToServer();

                }   // if


            } // onClick
        });

        //takePhoto Controller
        takePhotoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(intent, 0);

            }   // onclick
        });


    }   //Main Method

    @Override
    public void onBackPressed() {
         super.onBackPressed();
    }

    private void upLoadToServer() {

        upLoadImage();
        upDateString();

    }   // upload

    private void upDateString() {
        MyConstant myConstant = new MyConstant();
        MyUpdateJob myUpdateJob = new MyUpdateJob(PhotoActivity.this);
        myUpdateJob.execute(myConstant.getUrlEditJobString());


    }   // upDate

    private class MyUpdateJob extends AsyncTask<String, Void, String> {

        //Explicit
        private Context context;

        public MyUpdateJob(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {

            try {

                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = new FormEncodingBuilder()
                        .add("isAdd", "true")
                        .add("id", idjobString)
                        .add("Meter", meterString)
                        .add("ImageMeter", "http://swiftcodingthai.com/ry/Image" + imageNameString)
                        .build();
                Request.Builder builder = new Request.Builder();
                Request request = builder.url(strings[0]).post(requestBody).build();
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();


            } catch (Exception e) {
                Log.d("14novV1", "e doIn ==> " + e.toString());
            }

            return null;
        }   // doInBack

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("14novV1", "Result ==>" + s);

            if (Boolean.parseBoolean(s)) {
                finish();
            } else {
                Toast.makeText(context,"Cannot Update Value to Server",Toast.LENGTH_SHORT).show();
            }




        }   // onPost



    }   //MyUpdateJob Class


    private void upLoadImage() {

        //Change Policy
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy
                .Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        try {
            MyConstant myConstant = new MyConstant();
            SimpleFTP simpleFTP = new SimpleFTP();
            simpleFTP.connect(myConstant.getHostString(),
                    myConstant.getPortAnInt(),
                    myConstant.getUserFTPString(),
                    myConstant.getPasswordFTPString());
            simpleFTP.bin();
            simpleFTP.cwd("Image");
            simpleFTP.stor(new File(imagePathString));
            simpleFTP.disconnect();

            Toast.makeText(PhotoActivity.this, "Upload Picture Finish",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.d("8novV4", "e SimpleFTP ==> " + e.toString());

        }

    }   //upLoad

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == 0) && (resultCode == RESULT_OK)) {

            Log.d("8novV4", "Take Photo OK ");

            aBoolean = false;

            uri = data.getData();

            try {
                Bitmap bitmap = BitmapFactory
                        .decodeStream(getContentResolver()
                                .openInputStream(uri));
                showPhotoImageView.setImageBitmap(bitmap);


            } catch (Exception e) {
                e.printStackTrace();
            }

            // Find Image Path
            imagePathString = myFingpath(uri);
            Log.d("8novV4", "imagePath ==> " + imagePathString);

            // Find Image Name
            imageNameString = imagePathString.substring(imagePathString.lastIndexOf("/"));
            Log.d("8novV4", "imageName ==> " + imageNameString);

        }   // if

    }   // onActivityResult

    private String myFingpath(Uri uri) {

        String result = null;
        String[] strings = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri,strings, null, null, null);

        if (cursor != null) {

            cursor.moveToFirst();
            int i = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            result = cursor.getString(i);

        } else {
            result = uri.getPath();
        }

        return result;




    }
}   //Main Class
