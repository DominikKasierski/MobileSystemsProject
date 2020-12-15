package pl.ks.dk.covidapp.Fragments;

import pl.ks.dk.covidapp.Notifications.MyResponse;
import pl.ks.dk.covidapp.Notifications.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAtfZSKNM:APA91bFTKmCga3h7MC8JjsLWs3Ibbxr_4Sui6jKZl5mzTOhv3uhRbPF28wIFtFscda9XAWb-sDdVoJ8yV0fv2bj8yggZDh02ZgizXICOHaa9fGtfRvsFyjbo0HJYNaRM-uqwaZWU_Oo1"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
