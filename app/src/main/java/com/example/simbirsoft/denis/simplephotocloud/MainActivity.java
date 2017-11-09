package com.example.simbirsoft.denis.simplephotocloud;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    static final String myTag = "MY_TAG";

    @BindView(R.id.button_takeAndSend)
    Button buttonTakeAndSend;
    @BindView(R.id.progressBar_loadPercentage)
    ProgressBar loaderProgress;
    @BindView(R.id.imageView_imageFromCache)
    ImageView imageFromCache;

    File photoFile = null;
    Uri photoUri = null;

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
                printMyLog("File was created: " + photoFile.getPath());

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);

                printMyLog("Uri was created: " + photoUri.getPath());

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createPictureInCache() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
        String imageFileName = "TEMP_" + timeStamp + "_";
        File image = new File(getCacheDir(), imageFileName + ".jpg");
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            printMyLog("Request code = OK");
            imageFromCache.setImageBitmap(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
            printMyLog("File pathName from Uri: " + photoFile.getAbsolutePath());
            sendPictureToServer(photoFile);
        }
    }

    private void sendPictureToServer(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), file.getName());

        AccessInterface api = createRestService();
        Call<ResponseBody> response = api.uploadImage(body, name); //, progressPart);
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

    private void showToast(String param) {
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

    private void printMyLog(String message) {
        Log.d(myTag, message);
    }
}
