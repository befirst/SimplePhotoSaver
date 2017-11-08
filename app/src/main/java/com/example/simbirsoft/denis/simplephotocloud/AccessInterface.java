package com.example.simbirsoft.denis.simplephotocloud;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by user on 07.11.2017.
 */

public interface AccessInterface {

    String Base_URL = "https://android-cources.simbirsoft1.com/api/";

    String HEADERS =
            "Authorization: Basic NTlkZGI5YmFhNGJlMjA1ODZkMjYwZWExOjUzOWRiOTdkNmQ=" +
                    "X-Api-Factory-Application-Id: 59ddb9baa4be20586d260ea1" +
                    "Content-Type: application/json";

    @Headers({
            "X-Api-Factory-Application-Id:59ddb9baa4be20586d260ea1",
            "Authorization:Basic NTlkZGI5YmFhNGJlMjA1ODZkMjYwZWExOjUzOWRiOTdkNmQ=",
            //"Content-Type: multipart/form-data"
    })
    @Multipart
    @POST("db/photo")
    Call<ResponseBody> uploadImage(/*@Part MultipartBody.Part file,*/ @Part MultipartBody.Part progress);
}

