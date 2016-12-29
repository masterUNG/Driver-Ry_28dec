package nakthon.soraya.driverry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class ConfirmJob extends AppCompatActivity {

    //Explicit
    private String[] loginString;
    private String[] tagStrings = new String[]{"1decV1"};
    private Boolean aBoolean = true, restartABoolean = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_job);


        //Get Value of Login Pass
        loginString = getIntent().getStringArrayExtra("Login");
        for (int i = 0; i < loginString.length; i++) {
            Log.d("29decV1", "loginString(" + i + ")==>" + loginString[i]);
        }   // for
        //Login Status ==> 1
        editStatus(1);

        //Check Job
        checkJob();

    }   // Main Method

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("2decV2", "onRestart Work");

        if (restartABoolean) {
            Log.d("2decV2", "onRestart Work in Condition");

            //Edit Status Column to 2
            aBoolean = true;
            restartABoolean = false;

            try {

                //Update Status of jobTABLE
                EditStatusTo2 editStatusTo2 = new EditStatusTo2(ConfirmJob.this,
                        loginString[0], "1");   // เกิดจากการที่ คนขับรถ ปฎิเสธงาน
                editStatusTo2.execute();

                Log.d("29decV2", "Result jobTABLE ==> " + editStatusTo2.get());

                //Update Status of userTABLE_ry
                EditStatusDriver editStatusDriver = new EditStatusDriver(ConfirmJob.this,
                        loginString[0], "1");
                editStatusDriver.execute();
                Log.d("29decV2", "Result userTABLE ==> " + editStatusDriver.get());


            } catch (Exception e) {
                Log.d("2decV2", "e onRestate ==> " + e.toString());
            }


        }

    }   // onRestart

    private void checkJob() {

        //TodoIt
        try {

            Log.d("2decV1", "idDriver ที่ส่งไป ==> " + loginString[0]);

            MyCheckJob myCheckJob = new MyCheckJob(ConfirmJob.this, loginString[0], "0");
            myCheckJob.execute();
            String s = myCheckJob.get();
            Log.d("2decV1", "JSON ที่อ่านได้ ==> " + s);

            Log.d("2decV1", "Condition ที่เห็น ==> " + (!s.equals("null")));

            if (!s.equals("null")) {

                if (aBoolean) {
                    aBoolean = false;
                    myNotification();
                }

            } // if

        } catch (Exception e) {
            Log.d("1decV2", "e checkJob ==> " + e.toString());
        }


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkJob();

            }
        }, 1000);

    } // CheckJob

    private void myNotification() {

        Log.d("2decV1", "Notification Work");

        restartABoolean = true;

        Intent intent = new Intent(ConfirmJob.this, NotificationAlert.class);
        intent.putExtra("Login", loginString);

        PendingIntent pendingIntent = PendingIntent.getActivity(ConfirmJob.this,
                (int) System.currentTimeMillis(), intent, 0);

        Uri uri = RingtoneManager.getDefaultUri(Notification.DEFAULT_SOUND);

        Notification.Builder builder = new Notification.Builder(ConfirmJob.this);
        builder.setTicker("Driver ry");
        builder.setContentTitle("งานมาใหม่ค่ะ");
        builder.setContentText("กรุณาคลิ๊กที่นี้");
        builder.setSmallIcon(R.drawable.doremon48);
        builder.setSound(uri);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);

    }   //myNotification

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(tagStrings[0], "onStop");

        editStatus(0);

    }

    private void editStatus(int intStatus) {

        try {

            String s = null;
            EditStatusDriver editStatusDriver = new EditStatusDriver(ConfirmJob.this,
                    loginString[0], Integer.toString(intStatus));
            editStatusDriver.execute();

            if (Boolean.parseBoolean(editStatusDriver.get())) {
                s = "Change Status OK";
            } else {
                s = "Cannot Change Status";
            }

            Toast.makeText(ConfirmJob.this, s, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.d(tagStrings[0], "e editstatus ==> " + e.toString());

        }

    }
}   //Main Class
