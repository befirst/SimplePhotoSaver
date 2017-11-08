package com.example.simbirsoft.denis.simplephotocloud;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements ProgressRequestBody.UploadCallbacks {
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @BindView(R.id.button_takeAndSend)
    Button buttonTakeAndSend;
    @BindView(R.id.progressBar_loadPercentage)
    ProgressBar loaderProgress;

    File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loaderProgress.setProgress(0);
        buttonTakeAndSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createPictureInCache();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createPictureInCache() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
        String imageFileName = "TEMP_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                getApplicationContext().getCacheDir()    /* directory */
        );
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //if ((extras != null) && (extras.containsKey(MediaStore.EXTRA_OUTPUT))) {
                //try {
                    //File picture = new File(getRealPathFromURI((Uri)extras.getParcelable(MediaStore.EXTRA_OUTPUT)));
                    sendPictureToServer(photoFile);
                //} catch (Exception e) {
                   // e.printStackTrace();
               // }
            //}
        }
    }

    private void sendPictureToServer(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        //RequestBody description = RequestBody.create(MultipartBody.FORM, "TRARATATATARATATA");

        ProgressRequestBody progress = new ProgressRequestBody(file, this);
        MultipartBody.Part progressPart = MultipartBody.Part.createFormData("image", file.getName(), progress);

        AccessInterface api = createRestService();
        Call<ResponseBody> response = api.uploadImage(body); //, progressPart);
        response.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() != 200) {
                    Log.println(Log.INFO, "ResponseCode", String.valueOf(response.code() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"));
                    showToast(String.valueOf(response.code()));
                } else {
                    Log.println(Log.ERROR, "ResponseCode", "All is good!!!!!!!!!!!!!!!!!");
                    showToast("ALL IS GOOD!!!");
                    loaderProgress.setProgress(100, true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.println(Log.ERROR, "ResponseCode", t.getMessage());
                showToast("FAILURE!");
            }
        });
    }

    private void showToast(String param){
        Toast.makeText(getApplicationContext(), param, Toast.LENGTH_LONG).show();
    }

    private AccessInterface createRestService() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AccessInterface.Base_URL)
                .client(provideClient())
                .build();

        return retrofit.create(AccessInterface.class);
    }

    private OkHttpClient provideClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    @Override
    public void onProgressUpdate(int percentage) {
        loaderProgress.setProgress(percentage, true);
    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {

    }
}
