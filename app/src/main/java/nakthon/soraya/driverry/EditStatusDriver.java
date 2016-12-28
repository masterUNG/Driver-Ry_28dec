package nakthon.soraya.driverry;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * Created by DARK on 1/12/2559.
 */

public class EditStatusDriver extends AsyncTask<Void, Void, String> {
    private Context context;
    private static final String urlPHP = "http://swiftcodingthai.com/ry/edit_status_driver.php";
    private String idString, statusString;
    private static final String tag = "1decv1";

    public EditStatusDriver(Context context,
                            String idString,
                            String statusString) {
        this.context = context;
        this.idString = idString;
        this.statusString = statusString;
    }

    @Override
    protected String doInBackground(Void... voids) {

        try {

            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody requestBody = new FormEncodingBuilder()
                    .add("isAdd", "true")
                    .add("id", idString)
                    .add("Status", statusString)
                    .build();
            Request.Builder builder = new Request.Builder();
            Request request = builder.url(urlPHP).post(requestBody).build();
            Response response = okHttpClient.newCall(request).execute();
            return response.body().string();


        } catch (Exception e) {
            Log.d(tag, "e doIn ==> " + e.toString());
            return null;
        }


    }
}   // Main Class

