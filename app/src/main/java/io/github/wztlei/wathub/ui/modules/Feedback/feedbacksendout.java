package io.github.wztlei.wathub.ui.modules.Feedback;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface feedbacksendout {
    @POST()
    @FormUrlEncoded
    Call<Void> fbSend(
            @Field("entry.1393743862") String fb,
            @Field("entry.1667532236") String n,
            @Field("entry.740327582") String em);
}
