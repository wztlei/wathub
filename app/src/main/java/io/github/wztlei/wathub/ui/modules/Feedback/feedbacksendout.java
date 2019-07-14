package io.github.wztlei.wathub.ui.modules.Feedback;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface feedbacksendout {
    @POST("1FAIpQLSc3qx_rg_v6uZbzX6c0tw8uhOpsCRgAfH2-FiB1j0hLbIw_mA/viewform")
    @FormUrlEncoded
    Call<Void> fbSend(
            @Field("entry.1283738959") String fb,
            @Field("entry.1667532236") String n,
            @Field("entry.481583657") String em);
}
