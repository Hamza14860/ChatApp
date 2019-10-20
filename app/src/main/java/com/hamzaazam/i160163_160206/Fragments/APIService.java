package com.hamzaazam.i160163_160206.Fragments;

import com.hamzaazam.i160163_160206.Notifications.MyResponse;
import com.hamzaazam.i160163_160206.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAj3NIQvU:APA91bGQywSd7bAmXh7baQALFku8cqG_1ZeFXu9_keENnUZyAo7tbClEQuKU3wcheDIO1G5OabLT3mSak-633THX9quH7MPns-zuFhDizAQqvsaz6T82ENbWQDPk9_pG-AYTuPb29x3_"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
