package nakthon.soraya.driverry;

import android.content.Context;
import android.os.AsyncTask;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * Created by masterUNG on 1/19/2017 AD.
 */

public class UpdateCountMinus extends AsyncTask<Void, Void, String>{

    //Explicit
    private Context context;
    private static final String urlPHP = "http://swiftcodingthai.com/ry/edit_endTime_where_id_status.php";
    private String idDriverString, statusString, endCountTime, countTimeMinus;

    public UpdateCountMinus(Context context,
                            String idDriverString,
                            String statusString,
                            String endCountTime,
                            String countTimeMinus) {
        this.context = context;
        this.idDriverString = idDriverString;
        this.statusString = statusString;
        this.endCountTime = endCountTime;
        this.countTimeMinus = countTimeMinus;
    }

    @Override
    protected String doInBackground(Void... voids) {

        try {

            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody requestBody = new FormEncodingBuilder()
                    .add("isAdd", "true")
                    .add("ID_driver", idDriverString)
                    .add("Status", statusString)
                    .add("EndCountTime", endCountTime)
                    .add("CountTimeMinus", countTimeMinus)
                    .build();
            Request.Builder builder = new Request.Builder();
            Request request = builder.url(urlPHP).post(requestBody).build();
            Response response = okHttpClient.newCall(request).execute();
            return response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }
}   // Main Class
